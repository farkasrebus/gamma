package hu.bme.mit.gamma.modelica

import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition
import java.util.Map
import java.util.HashMap
import hu.bme.mit.gamma.statechart.statechart.Region
import hu.bme.mit.gamma.statechart.statechart.StateNode
import hu.bme.mit.gamma.statechart.statechart.State
import hu.bme.mit.gamma.statechart.statechart.RaiseEventAction
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import hu.bme.mit.gamma.statechart.statechart.StatechartModelPackage
import hu.bme.mit.gamma.statechart.interface_.EventTrigger
import hu.bme.mit.gamma.statechart.statechart.PortEventReference
import hu.bme.mit.gamma.statechart.statechart.EntryState
import java.util.Set
import java.util.HashSet
import hu.bme.mit.gamma.statechart.interface_.Port
import hu.bme.mit.gamma.statechart.statechart.Transition

class StatechartModel {
	public SynchronousStatechartDefinition model;
	public Map<String,Port> triggerEvents=new HashMap();
	public Map<String,Port> raisedEvents=new HashMap();
	public Map<Region,EntryState> entries=new HashMap();
	public Map<Region,Set<State>> normalStates=new HashMap();
	public Map<Region,Set<State>> compositeStates=new HashMap();
	public Map<Region,Set<Transition>> transitions=new HashMap();
	public Map<StateNode,Set<Transition>> incoming=new HashMap();
	public Map<StateNode,Set<Transition>> outgoing=new HashMap();
	
	
	new(SynchronousStatechartDefinition ssd) {
		model=ssd;
		for (r:model.regions) {
			findEvents(r)
			findStates(r)
		}
		findTriggerEvents()
		handleTransitions()
		
		//println(raisedEvents)
		//println(triggerEvents)
		//println(entries)
		//println(normalStates)
		//println(compositeStates)
	}
	
	def void handleTransitions() {
		for (t:model.transitions) {
			val src=t.sourceState;
			val r=src.eContainer as Region
			if (!transitions.containsKey(r)) transitions.put(r,new HashSet)
			transitions.get(r).add(t)
			if (!outgoing.containsKey(src)) outgoing.put(src,new HashSet)
			outgoing.get(src).add(t)
			
			val trg=t.targetState
			if (!incoming.containsKey(trg)) incoming.put(trg,new HashSet)
			incoming.get(trg).add(t)
		}
	}
	
	def void findStates(Region r) {
		val normals=new HashSet<State>();
		normalStates.put(r,normals);
		val comp=new HashSet<State>();
		compositeStates.put(r,comp);
		
		for (StateNode sn: r.stateNodes) {
			if (sn instanceof EntryState) {
				entries.put(r,sn)
			} else if (sn instanceof State) {
				if (sn.regions.size>0) {
					comp.add(sn)
					for (ir:sn.regions) {
						findStates(ir)
					}
				} else {
					normals.add(sn)
				}
			}
		}
	}
	
	def void findEvents(Region r) {
		for (StateNode sn: r.stateNodes) {
			if (sn instanceof State) {
				for (a:sn.entryActions) {
					if (a instanceof RaiseEventAction) {
						/*
						 * Gamma interface references are unresolved in the standalone ResourceSet.
						 * The token is retrieved directly from the parsed syntax tree.
						 */
						val node=NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT)
						raisedEvents.put(NodeModelUtils.getTokenText(node.first),a.port)
					}
				}
				for (Region ir: sn.regions) {
					findEvents(ir)
				}
			}
		}
	}
	
	//input events
	def void findTriggerEvents() {
		for (tran: model.transitions) {
			val trig=tran.trigger
			if (trig instanceof EventTrigger) {
				val er=trig.eventReference
				if (er instanceof PortEventReference){
					/*
					 * Gamma interface references are unresolved in the standalone ResourceSet.
					 * The token is retrieved directly from the parsed syntax tree.
					 */
					val node=NodeModelUtils.findNodesForFeature(er, StatechartModelPackage.Literals.PORT_EVENT_REFERENCE__EVENT)
					triggerEvents.put(NodeModelUtils.getTokenText(node.first), er.port)
				}
			}
		}
	}



	def createModelicaCode(String packageName)'''
package «packageName»

class «model.name»
import Modelica.StateGraph.InitialStep;
import Modelica.StateGraph.Step;
import Modelica.StateGraph.Transition;

«FOR e : triggerEvents.keySet()»
	Modelica.Blocks.Interfaces.BooleanInput «triggerEvents.get(e).name.toFirstLower»_«e»;
«ENDFOR»
«FOR e : raisedEvents.keySet()»
	Modelica.Blocks.Interfaces.BooleanOutput «raisedEvents.get(e).name.toFirstLower»_«e»;
«ENDFOR»

InitialStep «entries.get(model.regions.get(0)).name.toFirstLower»(nIn=0, nOut=1);
«FOR s:compositeStates.get(model.regions.get(0))»
«model.name»«s.name.toFirstUpper» «s.name.toFirstLower»;
Step «s.name.toFirstLower»Entry(nIn=«IF incoming.containsKey(s)»«incoming.get(s).size»«ELSE»0«ENDIF»,nOut=1);
«ENDFOR»
«FOR s:normalStates.get(model.regions.get(0))»
Step «s.name.toFirstLower»(nIn=«IF incoming.containsKey(s)»«incoming.get(s).size»«ELSE»0«ENDIF»,nOut=«IF outgoing.containsKey(s)»«outgoing.get(s).size»«ELSE»0«ENDIF»);
«ENDFOR»

end «model.name»;

end «packageName»;
'''
	

}