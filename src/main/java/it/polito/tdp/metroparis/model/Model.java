package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> grafo ;
	private List<Fermata> fermate ;
	private Map<Integer, Fermata> fermateIdMap ;
	
	public void creaGrafo() {
		
		// crea l'oggetto grafo
		this.grafo = new SimpleGraph<Fermata,DefaultEdge>(DefaultEdge.class) ;
		
		// aggiungi i vertici
		MetroDAO dao = new MetroDAO() ;
		this.fermate = dao.readFermate() ;
		
		fermateIdMap = new HashMap<>();
		for(Fermata f: this.fermate)
			this.fermateIdMap.put(f.getIdFermata(), f) ;
		
		Graphs.addAllVertices(this.grafo, this.fermate) ;
		
		// aggiungi gli archi
		
		// metodo 1: considero tutti i potenziali archi
		//UTILE SE POCHI NODI O QUERY TOTALE MOLTO COMPLESSA, UN ALTRO CASO N CUI CONVIENE E' QUANDO LE FERMATE HANNO NELL'OGGETTO GLIA' LE PROPRIETà PER DETERINARE I PESI O LA CONNESSIONE
//		long tic = System.currentTimeMillis();
//		for(Fermata partenza: this.grafo.vertexSet()) {
//			for(Fermata arrivo: this.grafo.vertexSet()) {
//				if(dao.isConnesse(partenza, arrivo)) {
//					this.grafo.addEdge(partenza, arrivo) ;
//				}
//			}
//		}
//		long toc = System.currentTimeMillis();
//		System.out.println("Elapsed time "+ (toc-tic));
		
		// metodo 2: data una fermata, trova la lista di quelle adiacente
		long tic = System.currentTimeMillis();
		for(Fermata partenza: this.grafo.vertexSet()) {
			List<Fermata> collegate = dao.trovaCollegate(partenza) ;
			
			for(Fermata arrivo: collegate) {
				this.grafo.addEdge(partenza, arrivo) ;
			}
		}
		long toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		// metodo 2A: data una fermata, troviamo la lista di id connessi, evito di creare una nuova classe ogni volta!
		tic = System.currentTimeMillis();
		for(Fermata partenza: this.grafo.vertexSet()) {
			List<Fermata> collegate = dao.trovaIdCollegate(partenza, fermateIdMap) ;
			
			for(Fermata arrivo: collegate) {
				this.grafo.addEdge(partenza, arrivo) ;
			}
		}
		toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		// metodo 3: faccio una query per prendermi tutti gli edges 
		
		tic = System.currentTimeMillis();
		List<coppieF> allCoppie = dao.getAllCoppie(fermateIdMap);
		for (coppieF coppia : allCoppie)
			this.grafo.addEdge(coppia.getPartenza(), coppia.getArrivo());
		
		toc = System.currentTimeMillis();
		System.out.println("Elapsed time "+ (toc-tic));
		
		
		System.out.println("Grafo creato con "+this.grafo.vertexSet().size() +
				" vertici e " + this.grafo.edgeSet().size() + " archi") ;
		System.out.println(this.grafo);
	}
	
	///////////////////////////////////
	////CALCOLO DEL PERCORSO MINIMO////
	///////////////////////////////////
	/* determina il percorso minimo tra le 2 fermate */
	
	
	public List<Fermata> percorso(Fermata partenza, Fermata arrivo) {
		// Visita il grafo partendo da 'partenza'
		BreadthFirstIterator<Fermata, DefaultEdge> visita = 
				new BreadthFirstIterator<>(this.grafo, partenza) ;
		List<Fermata> raggiungibili = new ArrayList<Fermata>() ;
		//itero sull'oggetto visita!
		while(visita.hasNext()) {
			Fermata f = visita.next() ;
//			raggiungibili.add(f) ;
		}
//		System.out.println(raggiungibili) ;
		
		// Trova il percorso sull'albero di visita
		//devo partire dal fondo!! altrimenti non so che ramo scegliere.
		List<Fermata> percorso = new ArrayList<Fermata>() ;
		Fermata corrente = arrivo ;
		percorso.add(arrivo) ;
		//il metodo getSpanningTreeEdge mi fornisce l'arco dell'albero che collega tutti i nodi con la visita in ampieza e che parte dal nodo come parametro
		DefaultEdge e = visita.getSpanningTreeEdge(corrente) ;
		while(e!=null) {
			//metodo che mi restituisce il vertic opposto
			Fermata precedente = Graphs.getOppositeVertex(this.grafo, e, corrente) ;
			percorso.add(0, precedente) ;
			corrente = precedente ;
			
			e = visita.getSpanningTreeEdge(corrente) ;
		}
		
		return percorso ;
	}
	
	public List<Fermata> getAllFermate(){
		MetroDAO dao = new MetroDAO() ;
		return dao.readFermate() ;
	}
	
	public boolean isGrafoLoaded() {
		return this.grafo.vertexSet().size()>0;
	}

}
