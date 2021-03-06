package controller.classes;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import controller.interfaces.Manager;
import exceptions.LowTrainCapacityException;
import exceptions.FullTrainException;
import exceptions.FullWarehouseException;
import model.interfaces.*;
import model.classes.*;

public class ManagerImpl implements Manager {
	
	/*
	 * Come specificato nella documentazione, la classe manager conterrà, rispettivamente, in primis il 
	 * riferimento alla classe stessa per poter sfruttare il SingleTon Design Pattern, dopodichè conterrà
	 * un set di tutti i direttori assunti, un set di tutte le richieste sottisfabili unicamente dal manager,
	 * un set di tutte le richieste attive attualmente, il riferimento al negozio ed il riferimento al treno
	 */
	private static final int MIN_QUANTITY = 100;
	private static ManagerImpl manager    = null;
	
	private Set<Director> linkDirectors;
	private Set<Request> linkRequestsManager;
	private Set<Request> linkGlobalRequests;
	private Train train;
	
	/**
	 * Il costruttore servirà principalmente a creare il treno con la capienza segnalata dall'utente
	 * ed inoltre servirà per inizializzare tutti i vari campi sopracitati.
	 * Il costruttore è inoltre privato per consentire l'utilizzo del SingleTon Design Pattern
	 * 
	 * @param la capacità del treno
	 */
	private ManagerImpl(int trainCapacity) {
		this.linkDirectors			= new LinkedHashSet<Director>();
		this.linkRequestsManager	= new LinkedHashSet<Request>();
		this.linkGlobalRequests 	= new LinkedHashSet<Request>();
		this.train 					= new TrainImpl(trainCapacity);
	}
	
	/*
	 * Sfruttando il SingleTon Design Pattern, necessiteremo di un metodo statico
	 * per l'allocazione della classe del manager e, dalla seconda invocazione, il metodo
	 * statico ci permetterà di avere il riferimento all'unica istanza del manager
	 *
	 * @param la capacità del treno
	 */
	public static ManagerImpl getManager(int trainCapacity) throws LowTrainCapacityException{
		if(trainCapacity < MIN_QUANTITY) {
			throw new LowTrainCapacityException("Low train capacity, please increase it.");
		}
		
		return manager;
	}
	
	
	public static ManagerImpl getManager() {
		return manager;
	}
	
	/*
	 * Viene passato un nome del direttore e ritorna il riferimento al direttore 
	 * 
	 * @param nome del direttore
	 * @return il direttore associato al nome
	 */
	private Director getDirectorByName(String directorName) {
		return this.linkDirectors.stream()
								 .filter(d -> d.getName().equals(directorName))
								 .findFirst()
								 .get();
	}
		
	/*
	 * Viene aggiunta la richiesta ai vari direttori che possono soddisfarla
	 * 
	 * @param quantità di materiale richiesto dall'utente
	 */
	private void sendRequest(Request request) {
		boolean satisfy = false;
		for (Director d : this.linkDirectors) {
			if(request.getSentMaterial().equals(/*d.getFactory().getMaterial()*/"llllll")) {
				d.addRequestToSatisfy(request);
				satisfy = true;
			}
		}
		
		if(satisfy)
			this.linkGlobalRequests.add(request);
		else
			this.linkRequestsManager.add(request);
	}
	
	/*
	 * Viene passato un riferimento all'oggetto direttore da aggiungere al set dei direttori
	 * 
	 * @param direttore assunto
	 */
	@Override
	public void hireDirector(Director hiredDirector) {
		this.linkDirectors.add(hiredDirector);
	}
	
	/*
	 * Viene passato il nome di un direttore da rimuovere dal set dei direttori
	 * 
	 * @param nome del direttore licenziato
	 */
	@Override
	public void fireDirector (String directorName) {
		this.linkDirectors.remove(getDirectorByName(directorName));		
	}

	/*
	 * Viene passata una richiesta che verrà successivamente inviata al treno 
	 * come carico merce e verrà rimossa da tutti i direttori che hanno tale richiesta
	 * 
	 * @param richiesta soddisfatta
	 */
	@Override
	public void satisfiesRequestDirector(Request requestApproved, String directorName) {
		requestApproved.setSendingFactory(getDirectorByName(directorName).getFactory());
		
		this.train.addRequest(requestApproved);
		
		this.linkDirectors.stream()
						  .filter(d -> d.getRequestsToSatisfy().contains(requestApproved))
						  .forEach(d -> d.removeRequestToSatisfy(requestApproved));
	}
	
	/*
	 * Viene passata una richiesta al Manager che verrà subito soddisfatta senza passare per il treno
	 * Successivamente verrà eliminata dalle richieste del Manager
	 * @param richiesta soddisfatta
	 */
	public void satisfiesRequestManager(Request requestApproved) throws FullWarehouseException{
		requestApproved.getSendingFactory().getLoadingWarehouse().addMaterial(requestApproved.getSentQuantity());
		linkRequestsManager.remove(requestApproved);
	}
	
		
	/*
	 * Prossima destinazione da raggiungere con il treno  
	 */
	@Override
	public void nextDestination() throws FullWarehouseException, FullTrainException {
		this.train.nextDestination();		
	}

	/*
	 * Metodo che servirà per creare una nuova richiesta grazie al direttore specificato
	 * 
	 * @param quantità di metariale richiesta
	 * @param nome del direttore
	 */
	@Override
	public void createNewRequest(int quantity, String directorName) {
		sendRequest(getDirectorByName(directorName).newRequest(quantity));
	}

	
	/*
	 * Metodo che permette di svuotare il magazzino con il materiale lavorato tramite il nome di un direttore
	 * 
	 * @param nome del direttore
	 */
	@Override
	public void emptyWarehouse(String directorName){
		this.getDirectorByName(directorName).emptyWarehouse();
	}

	/*
	 * Metodo che visualizza le informazioni riguardanti un direttore
	 * 
	 * @param nome del direttore
	 * @return direttore associato al nome
	 */
	@Override
	public Director showDirectorInfo(String directorName) {
		return getDirectorByName(directorName);	
	}
	
	/*
	 * Viene passato come parametro il nome di un direttore,
	 * verrà restituita l'azienda del direttore
	 * 
	 * @param nome del direttore da cui prendere l'azienda
	 * @return l'azienda associata al direttore
	 */
	@Override
	public Factory showFactoryInfo(String directorName) {
		return getDirectorByName(directorName).getFactory();
	}
	
	/*
	 * Metodo che visualizza le informazioni riguardanti il treno 
	 * 
	 * @return il treno
	 */
	
	@Override

	public Train showTrainInfo() {
		return this.train;
	}

	/*
	 * Metodo che visualizza le informazioni di una richiesta
	 * 
	 * @param ID della richiesta
	 * @return richiesta associata all'id
	 */
	@Override
	public Request showRequestInfo(int id){
		try {
			return this.show(id, this.linkGlobalRequests);
		} catch(NoSuchElementException e){
			return this.show(id, this.linkRequestsManager);
		}
	}
	
	/*
	 *  Metodo che ritorna una richiesta in base all'id e al set di richieste che gli viene passato
	 * 
	 *  @param ID della richiesta
	 *  @param set con le richieste
	 *  @return richiesta associata all'id e al tipo di set passatogli 
	 */
	private Request show(int id, Set<Request> set) throws NoSuchElementException{
		return set.stream()
				  .filter(r ->r.getRequestId() == (id))
				  .findFirst()
				  .get();		
	}
}
