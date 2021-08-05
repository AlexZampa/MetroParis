package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private Graph<Fermata, DefaultEdge> graph;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	
	public Model() {
		MetroDAO dao = new MetroDAO();
		
		this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		// CREAZIONE  VERTICI
		this.fermate = dao.getAllFermate();
		this.fermateIdMap = new HashMap<Integer, Fermata>();
		for(Fermata f : this.fermate) {
			this.fermateIdMap.put(f.getIdFermata(), f);
		}
		Graphs.addAllVertices(this.graph, this.fermate);
		
		
		// CREAZIONE ARCHI -- metodo 1 (coppie di vertici)
		/*
		for(Fermata fp : this.fermate) {				// partenza
			for(Fermata fa : this.fermate) {			// arrivo
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}
		*/
		
		// CREAZIONE ARCHI -- metodo 2 (da un vertice, trova tutti i connessi)
		/*
		for(Fermata fp : this.fermate) {
			List<Fermata> connesse = dao.fermateSuccesive(fp, this.fermateIdMap);
			for(Fermata fa : connesse) {
				this.graph.addEdge(fp, fa);
			}
		}
		*/
		
		// CREAZIONE ARCHI -- metodo 3 (chiedo al DB l'elenco degli archi)
		List<CoppiaFermate> coppie = dao.coppieFermate(this.fermateIdMap);
		for(CoppiaFermate c : coppie) {
			this.graph.addEdge(c.getFp(),c.getFa());
		}
		
		System.out.println(this.graph);
		System.out.format("Grafo caricato con %d vertici %d archi",
				this.graph.vertexSet().size(),
				this.graph.edgeSet().size());
	}
	
	public static void main(String args[]) {
		Model m = new Model();
		
	}
}
