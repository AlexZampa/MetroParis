package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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
		System.out.format("Grafo caricato con %d vertici %d archi\n",
				this.graph.vertexSet().size(),
				this.graph.edgeSet().size());
	}
	
	
	/**
	 * Visita il grafo con la strategia Breadth First
	 * e ritorna l'insieme di vertici incontrati.
	 * @param source {@link Fermata} di partenza
	 * @return {@link List} di {@link Fermata}
	 */
	public List<Fermata> visitaAmpiezza(Fermata source) {
		List<Fermata> visita = new ArrayList<Fermata>();
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.graph, source);
		
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		
		return visita;
	}
	
	
	/**
	 * Visita il grafo con la strategia Depth First
	 * e ritorna l'insieme di vertici incontrati.
	 * @param source {@link Fermata} di partenza
	 * @return {@link List} di {@link Fermata}
	 */
	public List<Fermata> visitaProfondita(Fermata source) {
		List<Fermata> visita = new ArrayList<Fermata>();
		GraphIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<>(this.graph, source);
		
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		
		return visita;
	}
	
	
	/**
	 * Crea albero visita
	 * @param source {@link Fermata} di partenza
	 * @return {@link Map} <{@link Fermata},{@link Fermata}>
	 */
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		Map<Fermata,Fermata> albero = new HashMap<>();
		albero.put(source, null);
		
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.graph, source);
		
		bfv.addTraversalListener(
				new TraversalListener<Fermata, DefaultEdge>() {
				
					@Override
					public void vertexTraversed(VertexTraversalEvent<Fermata> e) {}
					
					@Override
					public void vertexFinished(VertexTraversalEvent<Fermata> e) {}
					
					@Override
					public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
						// questo arco ha scoperto un nuovo vertice?
						DefaultEdge edge = e.getEdge();
						Fermata a = graph.getEdgeSource(edge);
						Fermata b = graph.getEdgeTarget(edge);
						if(albero.containsKey(a)) {
							albero.put(b, a);
						}
						else {
							albero.put(a, b);
						}
					}
					
					@Override
					public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
					
					@Override
					public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
			});
		
		
		while(bfv.hasNext()) {
			bfv.next();				// estrai elemento e prosegui
		}
		
		return albero;
	}
	
	
	
	public static void main(String args[]) {
		Model m = new Model();
		List<Fermata> visita1 = m.visitaAmpiezza(m.fermate.get(0));
		System.out.println(visita1);
		List<Fermata> visita2 = m.visitaProfondita(m.fermate.get(0));
		System.out.println(visita2);
		
		Map<Fermata, Fermata> albero = m.alberoVisita(m.fermate.get(0));
		for(Fermata f: albero.keySet()) {
			System.out.format("%s <- %s\n", f, albero.get(f));
		}
		
		
	}
}
