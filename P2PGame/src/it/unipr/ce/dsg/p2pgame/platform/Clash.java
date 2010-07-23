package it.unipr.ce.dsg.p2pgame.platform;

import java.util.ArrayList;


public class Clash {


//	public final static int HASH = 0;
//	public final static int DEFENSE = 1;
//	public final static int END = 2;
	enum Phase {
		HASH,
		DEFENSE,
		END
	};

	enum Result {
		WIN,
		LOSE
	};
//	public final static int WIN = 0;
//	public final static int LOSE = 1;


	private ArrayList<Object> myMoves;
	private ArrayList<Object> otherPlayerMoves;
	private ArrayList<String> hash;
	private ArrayList<Integer> results;
	private ArrayList<String> timing;

	private String otherPlayer;
	private String otherPlayerId;

	//private int statusLast;
	private Phase statusLast;

	public Clash(String otherPlayer, String otherPlayerId) {
		super();
		this.otherPlayer = otherPlayer;
		this.otherPlayerId = otherPlayerId;


		this.myMoves = new ArrayList<Object>();
		this.otherPlayerMoves = new ArrayList<Object>();
		this.results = new ArrayList<Integer>();
		this.hash = new ArrayList<String>();

	}

	public void addMyMove(Object move){
		this.myMoves.add(move);
	}

	public void addOtherPlayerMove(Object move){

		this.otherPlayerMoves.add(move);

		if (this.timing.size() != 0){
			long oldTime = Long.parseLong(this.timing.get(this.timing.size()-1));
			long time = System.currentTimeMillis() -  oldTime;

			this.timing.add(Long.toString(time));
		}

	}

	public void addResult(int res){
		this.results.add(res);
	}

	public void addHash(String hash){
		this.hash.add(hash);
	}

	public ArrayList<Object> getMyMoves() {
		return myMoves;
	}

	public void setMyMoves(ArrayList<Object> myMoves) {
		this.myMoves = myMoves;
	}

	public ArrayList<Object> getOtherPlayerMoves() {
		return otherPlayerMoves;
	}

	public void setOtherPlayerMoves(ArrayList<Object> otherPlayerMoves) {
		this.otherPlayerMoves = otherPlayerMoves;
	}

	public ArrayList<Integer> getResults() {
		return results;
	}

	public void setResults(ArrayList<Integer> results) {
		this.results = results;
	}

	public String getOtherPlayer() {
		return otherPlayer;
	}

	public void setOtherPlayer(String otherPlayer) {
		this.otherPlayer = otherPlayer;
	}

	public String getOtherPlayerId() {
		return otherPlayerId;
	}

	public void setOtherPlayerId(String otherPlayerId) {
		this.otherPlayerId = otherPlayerId;
	}



	public ArrayList<String> getTiming() {
		return timing;
	}

	//	public int getStatusLast() {
//		return statusLast;
//	}
	public Phase getStatusLast() {
		return statusLast;
	}

//	public void setStatusLast(int statusLast) {
//		this.statusLast = statusLast;
//	}
	public void setStatusLast(Phase statusLast) {
		this.statusLast = statusLast;
	}

	public ArrayList<String> getHash() {
		return hash;
	}

	public void setHash(ArrayList<String> hash) {
		this.hash = hash;
	}

	//TODO: per calcolare il vincitore
	public void closeClash(){
		int pos = this.myMoves.size();

		if (this.otherPlayerMoves.get(pos) instanceof Attack){
			Attack other = (Attack) this.otherPlayerMoves.get(pos);
			Defense my = (Defense) this.myMoves.get(pos);

			//TODO : calcolo del vincitore


		}
		else{
			Attack my = (Attack) this.otherPlayerMoves.get(pos);
			Defense other = (Defense) this.myMoves.get(pos);

			//TODO: calcolo del vincitore
		}
	}

	//TODO: verificare l'attacco che si era ricevuto precedentemente
	public boolean verifyAttack(){
		int pos = this.myMoves.size();
		Attack attack = (Attack) this.otherPlayerMoves.get(pos);
		if (attack.getHash().compareTo(this.getHash().get(pos)) == 0)
			return true;

		else
			return false;
	}
}