package graph;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DicoGraph<T extends Types, GE extends GraphElement> extends AbstractMap<T, Set<GE>> {
	private HashMap<T, Set<GE>> dico;
	
	public DicoGraph() {
		dico = new HashMap<>();
	}

	public boolean addValue(GE g) {
		@SuppressWarnings("unchecked")
		T type = (T) g.getType();
		if(!dico.containsKey(type) || dico.get(type) == null) {
			dico.put(type, new HashSet<GE>());
		}
		return dico.get(type).add(g);
	}
	
	public boolean removeValue(GE g) {
		@SuppressWarnings("unchecked")
		T type = (T) g.getType();
		if(!dico.containsKey(type) || dico.get(type) == null) {
			dico.put(type, new HashSet<GE>());
		}
		
		boolean output = dico.get(type).remove(g);
		
		if(dico.get(type).isEmpty()) {
			dico.remove(type);
		}
		
		return output;
	}

	public Set<GE> get(T key) {
		if(dico.containsKey(key) && dico.get(key)!=null) {
			return dico.get(key);
		}
		return new HashSet<GE>();
	}
	
	@Override
	public Set<Entry<T, Set<GE>>> entrySet() {
		return dico.entrySet();
	}
}
