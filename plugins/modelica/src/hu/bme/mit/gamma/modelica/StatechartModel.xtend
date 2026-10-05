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

class StatechartModel {
	public SynchronousStatechartDefinition model;
	public Map<String,String> triggerEvents=new HashMap();
	public Map<String,String> raisedEvents=new HashMap();
	
	new(SynchronousStatechartDefinition ssd) {
		model=ssd;
		for (r:model.regions) {
			findEntryEvents(r)
		}
		findTriggerEvents()
		//println(raisedEvents)
		//println(triggerEvents)
	}
	
	def void findEntryEvents(Region r) {
		for (StateNode sn: r.stateNodes) {
			if (sn instanceof State) {
				for (a:sn.entryActions) {
					if (a instanceof RaiseEventAction) {
						/*
						 * Gamma interface references are unresolved in the standalone ResourceSet.
						 * The token is retrieved directly from the parsed syntax tree.
						 */
						val node=NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT)
						raisedEvents.put(NodeModelUtils.getTokenText(node.first),a.port.name)
					}
				}
				for (Region ir: sn.regions) {
					findEntryEvents(ir)
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
					triggerEvents.put(NodeModelUtils.getTokenText(node.first), er.port.name)
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
	Modelica.Blocks.Interfaces.BooleanInput «triggerEvents.get(e).toFirstLower»_«e»;
«ENDFOR»
«FOR e : raisedEvents.keySet()»
	Modelica.Blocks.Interfaces.BooleanOutput «raisedEvents.get(e).toFirstLower»_«e»;
«ENDFOR»


end «model.name»;

end «packageName»;
'''
	

}