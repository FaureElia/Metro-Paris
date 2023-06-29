package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultWeightedEdge> grafo ;
	private List<Fermata> fermate ;
	private Map<Integer, Fermata> fermateIdMap ;
	
	//cammini minimi
	private Double shortestPathValue;
	private Double casualPathValue;
	
	public Double getShortestPathValue() {
		return shortestPathValue;
	}

	public void creaGrafo() {
		
		// crea l'oggetto grafo
		this.grafo = new SimpleWeightedGraph<Fermata, DefaultWeightedEdge>(DefaultWeightedEdge.class) ;
		
		// aggiungi i vertici
		MetroDAO dao = new MetroDAO() ;
		this.fermate = dao.readFermate() ;
		
		fermateIdMap = new HashMap<>();
		for(Fermata f: this.fermate)
			this.fermateIdMap.put(f.getIdFermata(), f) ;
		
		Graphs.addAllVertices(this.grafo, this.fermate) ;
				
		// metodo 3: faccio una query per prendermi tutti gli edges 
		
		List<coppieF> allCoppie = dao.getAllCoppie(fermateIdMap);
		for (coppieF coppia : allCoppie) {
//			DefaultWeightedEdge e = this.grafo.addEdge(coppia.getPartenza(), coppia.getArrivo());
//			if (e!=null)
//				this.grafo.setEdgeWeight(e, distanza);
			
			double distanza = LatLngTool.distance(coppia.getPartenza().getCoords(),
					coppia.getArrivo().getCoords(), LengthUnit.METER) ;
			
			Graphs.addEdge(this.grafo, coppia.getPartenza(), coppia.getArrivo(), distanza) ;
//			Graphs.addEdge(this.grafo, coppia.getPartenza(), coppia.getArrivo(), 1.0) ;
		}
		
		
		
		System.out.println("Grafo creato con "+this.grafo.vertexSet().size() +
				" vertici e " + this.grafo.edgeSet().size() + " archi") ;
		System.out.println(this.grafo);
	}
	
	/* determina il percorso MINIMO tra le 2 fermate */
	//se ho un grafico pesato, non posso utilizzare BreadthFirst iterator oppure DepthFirstIterator!
	//Devo utilizzare algoritmo di Dijkstra
	public List<Fermata> percorsoMinimo(Fermata partenza, Fermata arrivo) {

		DijkstraShortestPath<Fermata, DefaultWeightedEdge> sp = 
				new DijkstraShortestPath<>(this.grafo) ;
		
		GraphPath<Fermata, DefaultWeightedEdge> gp = sp.getPath(partenza, arrivo) ;
		this.shortestPathValue=gp.getWeight();
		
		return gp.getVertexList() ;
	}
	
	//DETERMINA UN PERCORSO CASUALE TRA DUE NODI
	
	public List <Fermata> percorsoCasuale(Fermata partenza, Fermata arrivo){
		BreadthFirstIterator<Fermata, DefaultWeightedEdge> iteratore=new BreadthFirstIterator<>(this.grafo,partenza);
		List<Fermata> percorso=new ArrayList<>();
		while(iteratore.hasNext()) {
			Fermata f=iteratore.next();
			percorso.add(f);
			if(f.equals(arrivo)) {
				System.out.println("fermato in anticipo");
				break;
				
			}	
		}
		if(!percorso.contains(arrivo)) {
			return null;
		}
		
		//esploro
		List<Fermata> cammino=new ArrayList<>();
		Fermata current=arrivo;
		this.casualPathValue=0.0;
		DefaultWeightedEdge e=iteratore.getSpanningTreeEdge(arrivo);
		cammino.add(current);
		while(e!=null) {
			this.casualPathValue+=this.grafo.getEdgeWeight(e);
			current=Graphs.getOppositeVertex(this.grafo, e, current);
			cammino.add(0,current);
			e=iteratore.getSpanningTreeEdge(current);	
		}
		return cammino;
	}
	
	
	public Double getCasualPathValue() {
		return casualPathValue;
	}

	public void setCasualPathValue(Double casualPathValue) {
		this.casualPathValue = casualPathValue;
	}

	public List<Fermata> getAllFermate(){
		MetroDAO dao = new MetroDAO() ;
		return dao.readFermate() ;
	}
	
	public boolean isGrafoLoaded() {
		return this.grafo.vertexSet().size()>0;
	}

	public String getShortestValue() {
		
		return null;
	}

}
