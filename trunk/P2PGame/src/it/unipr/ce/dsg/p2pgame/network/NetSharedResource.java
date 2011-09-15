package it.unipr.ce.dsg.p2pgame.network;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Shared resource among all thread of a peer.
 * Can store information about PeerId and NetPeerInfo
 *
 * @author Stefano Sebastio (stefano.sebastio@studenti.unipr.it)
 *
 */
public class NetSharedResource {

	/**
	 * Store peer's informations
	 */
	private ConcurrentHashMap<String, NetPeerInfo> peersInfo;

	/**
	 * Store peer's identifier
	 */
	private ConcurrentHashMap<String, String> peersId;

	/**
	 *
	 * Constructor for shared resource. Initialize all.
	 *
	 */
	public NetSharedResource() {
		super();

		this.peersInfo = new ConcurrentHashMap<String, NetPeerInfo>();
		this.peersId = new ConcurrentHashMap<String, String>();
	}


	/**
	 *
	 * Get all shared information about peer
	 *
	 * @return the HashMap with all peer info
	 *
	 */
	public ConcurrentHashMap<String, NetPeerInfo> getPeersInfo() {
		return peersInfo;
	}


	/**
	 *
	 * Get all shared identifier about peer
	 *
	 * @return the HashMap with all peer identifier
	 *
	 */
	public ConcurrentHashMap<String, String> getPeersId() {
		return peersId;
	}

	/**
	 *
	 * Get identifier information saved for 'owner'
	 *
	 * @param owner the identifier of cache owner
	 * @return the peer identifier saved
	 *
	 */
	public String getIdFor(String owner){
		return this.peersId.get(owner);
	}

	/**
	 *
	 * Get peer information saved for 'owner'
	 *
	 * @param owner the identifier of cache owner
	 * @return the peer information saved
	 *
	 */
	public NetPeerInfo getInfoFor(String owner){
		//System.out.println("############NET SHARED REOUSRCE GETINFOFOR#############à");
		return this.peersInfo.get(owner);
	}

	/**
	 *
	 * Save new identifier and information on 'owner' cache
	 *
	 * @param owner the owner of cache to update
	 * @param npi the information about peer to save
	 * @param pId the identifier about peer to save
	 *
	 */
	public synchronized void saveInfo(String owner, NetPeerInfo npi, String pId){

		
		if (this.peersInfo.containsKey(owner)){
			this.peersInfo.remove(owner);
			this.peersId.remove(pId);
		}

		this.peersInfo.put(owner, npi);
		this.peersId.put(owner, pId);
		
	/*	if(!this.peersInfo.containsKey(owner))
		{
			this.peersInfo.put(owner, npi);
			this.peersId.put(owner, pId);
			
			
		}
*/
	}
	
	public void printPeersInfo()
	{
		Set<String> key_set=peersInfo.keySet();
		Iterator<String> iterator=key_set.iterator();
		int i=0; 
		while(iterator.hasNext())
		{
			
			String key=iterator.next();
			NetPeerInfo info=peersInfo.get(key);
			System.out.println("PEER INFO "+i+" "+key +" : "+ info.getIpAddress()+" "+info.getPortNumber() );
			i++;
		}
	}


}
