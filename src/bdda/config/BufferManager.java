package bdda.config;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class BufferManager {
	/** Configuration de la base de données (référence ou copie selon besoin).
	 * Note: en Java les objets sont référencés — ici on conserve une référence
	 * vers la configuration. */
	private DBConfig dbConfig;

	/** Référence vers l'instance de DiskManager fournie. NE PAS DUPLIQUER/CLONER. */
	private DiskManager diskManager;

	/**
	 * Constructeur principal. Stocke une référence vers la configuration et une
	 * référence vers l'instance de DiskManager (ne clone pas DiskManager).
	 *
	 * @param dbConfig instance de configuration (peut être conservée par référence)
	 * @param diskManager instance de DiskManager à utiliser (ne pas dupliquer)
	 * @throws IllegalArgumentException si un argument est null
	 */
	public BufferManager(DBConfig dbConfig, DiskManager diskManager) {
		if (dbConfig == null) {
			throw new IllegalArgumentException("dbConfig ne peut pas être null");
		}
		if (diskManager == null) {
			throw new IllegalArgumentException("diskManager ne peut pas être null");
		}
		// On conserve simplement les références (pas de clone de DiskManager)
		this.dbConfig = dbConfig;
		this.diskManager = diskManager;
	}

	/**
	 * Retourne la configuration de la base.
	 *
	 * @return instance de DBConfig
	 */
	public DBConfig getDbConfig() {
		return this.dbConfig;
	}

	/**
	 * Retourne le DiskManager associé.
	 *
	 * @return instance de DiskManager
	 */
	public DiskManager getDiskManager() {
		return this.diskManager;
	}

	/**
	 *  Délègue l'initialisation au DiskManager. 
	 */
	public void init() {
		this.diskManager.init();
		// initialise le pool de buffers gérés par le BufferManager
		int numBuffers = DEFAULT_NUM_BUFFERS;
		int pageSize = this.dbConfig.getPageSize();
		this.frames = new ArrayList<>(numBuffers);
		for (int i = 0; i < numBuffers; i++) {
			BufferFrame f = new BufferFrame();
			f.buffer = ByteBuffer.allocate(pageSize);
			f.pageID = null; // non utilisé
			f.dirty = false;
			f.lastUsed = 0L;
			f.pinCount = 0;
			this.frames.add(f);
		}
	}

	/** 
	 * Délègue la sauvegarde / fin au DiskManager. 
	 */
	public void finish() {
		// flush all dirty buffers before finishing
		for (BufferFrame f : this.frames) {
			if (f != null && f.pageID != null && f.dirty) {
				flushFrame(f);
			}
		}
		this.diskManager.finish();
	}

	/** 
	 * Alloue une page en déléguant au DiskManager.
	 * 
	 * @return identifiant de la page allouée
	 */
	public PageID allocPage() {
		return this.diskManager.allocPage();
	}

	/** 
	 * Désalloue une page en la passant au DiskManager. 
	 * 
	 * @param pageID identifiant de la page à désallouer
	 */
	public void deAllocPage(PageID pageID) {
		this.diskManager.deAllocPage(pageID);
	}

	/** 
	 * Lit une page en déléguant au DiskManager. 
	 * 
	 * * @param pageID identifiant de la page
	 * @param buff buffer destination
	 */
	public void readPage(PageID pageID, ByteBuffer buff) {
		this.diskManager.readPage(pageID, buff);
	}

	/** 
	 * Écrit une page en déléguant au DiskManager. 
	 * 
	 * @param pageID identifiant de la page
	 * @param buff buffer source
	 */
	public void writePage(PageID pageID, ByteBuffer buff) {
		this.diskManager.writePage(pageID, buff);
	}

	// Pool interne et politique de remplacement

	/** Nombre de buffers par défaut */
	private static final int DEFAULT_NUM_BUFFERS = 5;

	/** Liste des frames (buffers) gérés par le BufferManager */
	private List<BufferFrame> frames;

	/** Compteur simple pour implémenter LRU via timestamp croissant */
	private long usageCounter = 1L;

	/** Représente un frame de buffer dans le pool */
	private static class BufferFrame {
		PageID pageID;
		ByteBuffer buffer;
		boolean dirty;
		long lastUsed; // plus grand => plus récemment utilisé
		int pinCount; // nombre de pins (appels getPage non relachés)
	}

	/**
	 * Récupère (ou remplace) un buffer contenant la page demandée.
	 * Politique de remplacement: LRU basique.
	 *
	 * @param pageID id de la page demandée
	 * @return ByteBuffer géré par le BufferManager contenant la page (prêt en lecture)
	 */
	public synchronized ByteBuffer getPage(PageID pageID) {
		if (pageID == null) {
			throw new IllegalArgumentException("pageID ne peut pas être null");
		}
		// cherche si la page est déjà en cache
		BufferFrame freeFrame = null;
		long oldest = Long.MAX_VALUE;
		for (BufferFrame f : this.frames) {
			if (f.pageID != null && f.pageID.getFileIdx() == pageID.getFileIdx()
					&& f.pageID.getPageIdx() == pageID.getPageIdx()) {
				// hit
				f.lastUsed = ++this.usageCounter;
				// incrémente le pin_count car l'appelant obtient une référence au buffer
				f.pinCount = f.pinCount + 1;
				// positionner le buffer pour lecture par l'appelant
				f.buffer.rewind();
				return f.buffer;
			}
			if (f.pageID == null && freeFrame == null) {
				freeFrame = f;
			}
			// On ne choisit comme candidat LRU qu'un frame non piné
			if (f.pinCount == 0 && f.lastUsed < oldest) {
				oldest = f.lastUsed;
			}
		}

		// si page non en cache, choisir un frame libre ou un victim selon la politique
		BufferFrame target = null;
		if (freeFrame != null) {
			target = freeFrame;
		} else {
			// choisir un victim non piné selon la politique courante
			if (this.currentPolicy == ReplacementPolicy.MRU) {
				// MRU: choisir le plus récent (max lastUsed)
				long newest = Long.MIN_VALUE;
				for (BufferFrame f : this.frames) {
					if (f.pinCount == 0 && f.lastUsed > newest) {
						newest = f.lastUsed;
						target = f;
					}
				}
			} else {
				// LRU par défaut: plus ancien (min lastUsed)
				long oldest2 = Long.MAX_VALUE;
				for (BufferFrame f : this.frames) {
					if (f.pinCount == 0 && f.lastUsed < oldest2) {
						oldest2 = f.lastUsed;
						target = f;
					}
				}
			}
		}
		if (target == null) {
			throw new IllegalStateException("Aucun frame disponible dans le pool");
		}

		// si target contient une page et est dirty -> flush
		if (target.pageID != null && target.dirty) {
			flushFrame(target);
		}

		// remplir le buffer avec la page demandée via DiskManager
		target.pageID = pageID;
		target.buffer.clear();
		this.diskManager.readPage(pageID, target.buffer);
		// après lecture, DiskManager fait flip(); nous repositionnons au début
		target.buffer.rewind();
		target.dirty = false;
		// le buffer est maintenant détenu par l'appelant -> pinCount = 1
		target.pinCount = 1;
		target.lastUsed = ++this.usageCounter;
		return target.buffer;
	}

	/** 
	 * Ecrit le contenu du frame sur le disque via DiskManager 
	 * 
	 * @param f buffer à écrire
	 */
	private void flushFrame(BufferFrame f) {
		if (f == null || f.pageID == null) return;
		try {
			// prépare buffer pour écriture: DiskManager.writePage attend un buffer prêt à être lu
			f.buffer.rewind();
			this.diskManager.writePage(f.pageID, f.buffer);
			f.dirty = false;
		} catch (Exception e) {
			System.err.println("Erreur lors du flush d'un frame: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// FreePage, politiques et flush global

	/** Représentation simple des politiques de remplacement supportées */
	public enum ReplacementPolicy {
		LRU,
		MRU,
	}

	/** Politique courante ; LRU par défaut */
	private ReplacementPolicy currentPolicy = ReplacementPolicy.LRU;

	/** Change la politique de remplacement courante. Prioritaire sur la DBConfig. */
	public synchronized void SetCurrentReplacementPolicy(ReplacementPolicy policy) {
		if (policy == null) throw new IllegalArgumentException("policy ne peut pas être null");
		this.currentPolicy = policy;
	}

	/**
	 * Libère une page précédemment récupérée par getPage. Ne doit appeler aucun
	 * DiskManager. Décrémente le pin_count et met à jour le flag dirty si demandé.
	 *
	 * @param pageID page à libérer
	 * @param valdirty si true, marque le buffer comme dirty
	 */
	public synchronized void FreePage(PageID pageID, boolean valdirty) {
		if (pageID == null) return;
		for (BufferFrame f : this.frames) {
			if (f.pageID != null && f.pageID.getFileIdx() == pageID.getFileIdx()
					&& f.pageID.getPageIdx() == pageID.getPageIdx()) {
				// met à jour dirty
				if (valdirty) f.dirty = true;
				// décrémente pinCount sans aller en dessous de 0
				if (f.pinCount > 0) {
					f.pinCount--;
				}
				// mise à jour éventuelle des infos de la politique
				if (f.pinCount == 0) {
					f.lastUsed = ++this.usageCounter;
				}
				return;
			}
		}
		// si on n'a pas trouvé la page, on ignore (ou on pourrait lever une erreur)
	}

	/**
	 * Ecrit toutes les pages dirty sur disque (via DiskManager) puis remet le pool
	 * dans l'état initial (comme après init()).
	 */
	public synchronized void FlushBuffers() {
		// écrire les buffers dirty
		for (BufferFrame f : this.frames) {
			if (f != null && f.pageID != null && f.dirty) {
				flushFrame(f);
			}
		}
		// remettre le pool à l'état initial : aucun contenu chargé
		for (BufferFrame f : this.frames) {
			if (f == null) continue;
			f.pageID = null;
			f.dirty = false;
			f.pinCount = 0;
			f.lastUsed = 0L;
			// clear le buffer (remet position à 0, limite à capacity)
			f.buffer.clear();
		}
	}
}
