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
	
	/* determina il percorso minimo tra le 2 fermate */
	//se ho un grafico pesato, non posso utilizzare BreadthFirst iterator oppure DepthFirstIterator!
	//Devo utilizzare algoritmo di Dijkstra
	public List<Fermata> percorso(Fermata partenza, Fermata arrivo) {

		DijkstraShortestPath<Fermata, DefaultWeightedEdge> sp = 
				new DijkstraShortestPath<>(this.grafo) ;
		
		GraphPath<Fermata, DefaultWeightedEdge> gp = sp.getPath(partenza, arrivo) ;
		
		return gp.getVertexList() ;
	}
	
	public List<Fermata> getAllFermate(){
		MetroDAO dao = new MetroDAO() ;
		return dao.readFermate() ;
	}
	
	public boolean isGrafoLoaded() {
		return this.grafo.vertexSet().size()>0;
	}

}
