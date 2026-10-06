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
import hu.bme.mit.gamma.statechart.statechart.TimeoutEventReference
import java.util.List
import java.util.ArrayList

class StatechartModel {
	public SynchronousStatechartDefinition model;
	public Map<String,Port> eventPorts=new HashMap();
	public Map<Transition,String> triggerEvents=new HashMap();
	public Map<State,String> raisedEvents=new HashMap();
	public Map<Region,EntryState> entries=new HashMap();
	public Map<Region,Set<State>> normalStates=new HashMap();
	public Map<Region,Set<State>> compositeStates=new HashMap();
	public Map<Region,Set<Transition>> transitions=new HashMap();
	public Map<StateNode,List<Transition>> incoming=new HashMap();
	public Map<StateNode,List<Transition>> outgoing=new HashMap();
	val transitionNames = new HashMap<Transition, String>()
	var transitionCounter = 0
	
	
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
			if (!outgoing.containsKey(src)) outgoing.put(src,new ArrayList)
			outgoing.get(src).add(t)
			
			val trg=t.targetState
			if (!incoming.containsKey(trg)) incoming.put(trg,new ArrayList)
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
						val event=NodeModelUtils.getTokenText(node.first)
						eventPorts.put(event,a.port)
						raisedEvents.put(sn,event)
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
					val event=NodeModelUtils.getTokenText(node.first)
					eventPorts.put(event, er.port)
					triggerEvents.put(tran,event)
				}
			}
		}
	}

	def getTransitionName(Transition transition) {
        transitionNames.computeIfAbsent(transition) [
            transitionCounter++
            "t" + transitionCounter
        ]
    }


	def createModelicaCode(String packageName)'''
package «packageName»

class «model.name»
import Modelica.StateGraph.InitialStep;
import Modelica.StateGraph.Step;
import Modelica.StateGraph.Transition;

«FOR e : triggerEvents.values.toSet»
Modelica.Blocks.Interfaces.BooleanInput «e.modelicaName»;
«ENDFOR»
«FOR e : raisedEvents.values.toSet»
Modelica.Blocks.Interfaces.BooleanOutput «e.modelicaName»;
«ENDFOR»

 InitialStep «entries.get(model.regions.get(0)).name.toFirstLower»(nIn=0, nOut=1);
 «FOR s:compositeStates.get(model.regions.get(0))»
 «model.name»«s.name.toFirstUpper» «s.name.toFirstLower»;
 Step «s.name.toFirstLower»Entry(nIn=«IF incoming.containsKey(s)»«incoming.get(s).size»«ELSE»0«ENDIF»,nOut=1);
 Transition entryTo«s.name.toFirstUpper»(enableTimer=true, waitTime=0);
 «ENDFOR»
 «FOR s:normalStates.get(model.regions.get(0))»
 Step «s.name.toFirstLower»(nIn=«IF incoming.containsKey(s)»«incoming.get(s).size»«ELSE»0«ENDIF»,nOut=«IF outgoing.containsKey(s)»«outgoing.get(s).size»«ELSE»0«ENDIF»);
 «ENDFOR»
 «FOR t:transitions.get(model.regions.get(0))»
 Transition «t.transitionName» «IF t.isDelayed»(enableTimer=true, waitTime=«t.getDelay»«ELSE»(condition=«triggerEvents.get(t).modelicaName»«ENDIF»);
 «ENDFOR»

 initial equation
 «FOR e : raisedEvents.values.toSet»
 «e.modelicaName»=false;
 «ENDFOR»

equation
  «FOR s:compositeStates.get(model.regions.get(0))»
  connect(«s.name.toFirstLower»Entry.outPort[1],entryTo«s.name.toFirstUpper».inPort);
  connect(entryTo«s.name.toFirstUpper».outPort,«s.name.toFirstLower».inPort);
  «ENDFOR»
  «FOR t:transitions.get(model.regions.get(0))»
  connect(«t.sourceState.name.toFirstLower».«IF 
  	t.sourceState.isComposite»suspend«ELSE»outPort«ENDIF»[«outgoing.get(t.sourceState).indexOf(t)+1»],«t.transitionName».inPort);
  connect(«t.transitionName».outPort,«t.targetState.name.toFirstLower»«IF 
  	t.targetState.isComposite»Entry«ENDIF».inPort[«incoming.get(t.targetState).indexOf(t)+1»]);
  «ENDFOR»
  
«/*TODO: handle input/outputs between states AND raised events by transitions and entries*/»
  
  

end «model.name»;


«/*TODO: create classes for composite states*/»

end «packageName»;
'''
	
	def boolean isComposite(StateNode state) {
		for (c:compositeStates.values) {
			if (c.contains(state)) return true
		}
		return false
	}

/* «»
 * 
 */
	
	def getModelicaName(String event) {
		return eventPorts.get(event).name.toFirstLower+"_"+event
	}
	
	def int getDelay(Transition t) {
		if (t.sourceState instanceof EntryState) {
			return 0
		} else {
			val et=t.trigger as EventTrigger
			val to=et.eventReference as TimeoutEventReference
			println(to.timeout.name)//TODO!!!
		}
		return 0
	}
	
	def boolean isDelayed(Transition t) {
		if (t.sourceState instanceof EntryState) {
			return true
		} else {
			val trig=t.trigger
			if (trig instanceof EventTrigger) {
				if (trig.eventReference instanceof TimeoutEventReference) {
					return true
				}
			}
		}
		return false
	}
	

}