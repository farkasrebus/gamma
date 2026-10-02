package hu.bme.mit.gamma.modelica

import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.common.util.URI
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage
import org.eclipse.emf.ecore.EObject
import hu.bme.mit.gamma.statechart.interface_.Package
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition
import hu.bme.mit.gamma.statechart.interface_.Port
import hu.bme.mit.gamma.statechart.interface_.RealizationMode
import hu.bme.mit.gamma.statechart.interface_.EventTrigger
import hu.bme.mit.gamma.statechart.statechart.PortEventReference
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import hu.bme.mit.gamma.statechart.statechart.StatechartModelPackage
import java.util.HashSet
import hu.bme.mit.gamma.statechart.statechart.Region
import hu.bme.mit.gamma.statechart.statechart.StateNode
import java.util.Set
import hu.bme.mit.gamma.statechart.statechart.State
import hu.bme.mit.gamma.action.model.Action
import hu.bme.mit.gamma.statechart.statechart.RaiseEventAction

class Main {
	def static void main(String[] args) {
		//println("Hello Gamma")
		StatechartLanguageStandaloneSetup.doSetup()
		
		val package = InterfaceModelPackage.eINSTANCE
		
		
		val resourceSet = new ResourceSetImpl
		val resource = resourceSet.getResource(
            URI.createFileURI("D:/git/gamma/tutorial/hu.bme.mit.gamma.tutorial.finish/model/TrafficLight/TrafficLightCtrl.gcd"),
            //URI.createFileURI("D:/git/gamma/tutorial/hu.bme.mit.gamma.tutorial.finish/model/Crossroad.gcd"),
            true
        )
		EcoreUtil.resolveAll(resourceSet)
		
		if (resource.contents.size>1) println("Only one root is supported.")
		
		val root=resource.contents.first
		if (root instanceof Package) {
			val pname=root.name;
			
			if (root.components.size!=1) println("Only one component (Statechart) is supported")
			val statechart=root.components.first
			if (statechart instanceof SynchronousStatechartDefinition) {
				val code=createModelicaCode(statechart,pname)
				print(code)
				/*println(statechart.annotations)
				println(statechart.functionDeclarations)
				println(statechart.guardEvaluation)//on the fly?
				println(statechart.invariants)
				println(statechart.orthogonalRegionSchedulingOrder)//sequential?
				println(statechart.parameterDeclarations)*/
				//println(statechart.ports)//ports!!!!!!!!!
				
				for (Region r:statechart.regions) {
					val raisedEvents=new HashSet()
					getEntryEvents(r,raisedEvents)
					print(raisedEvents)
				}
				
				//println(statechart.regions)//region
				/*println(statechart.schedulingOrder)//top down
				println(statechart.timeoutDeclarations)//timeouts!!!
				println(statechart.transitionPriority)//OFF
				println(statechart.transitions)//trans
				println(statechart.variableDeclarations)//*/
			} else println("Only synchronous statecharts are supported")
			
			
		} else println("Unexpected root. Expected type: Package")
		
		printTree(root,"")
	}
	
	def static getEntryEvents(Region r,Set<String> result) {
		for (StateNode sn: r.stateNodes) {
			println(sn)
			if (sn instanceof State) {
				for (Action a:sn.entryActions) {
					if (a instanceof RaiseEventAction) {
						val node=NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT)
						result.add(a.port.name+"_"+NodeModelUtils.getTokenText(node.first))
					}
				}
				for (Region ir: sn.regions) {
					getEntryEvents(ir,result)
				}
			}
		}
	}
	
	def static collectEvent(Action a, Set<String> result) {
		
	}
	
	def static isRequired(Port p) {
		return p.interfaceRealization.realizationMode == RealizationMode.REQUIRED;
	}
	
	//input events
	def static getTriggerEvents(SynchronousStatechartDefinition sct) {
		val result=new HashSet<String>();
		for (tran: sct.transitions) {
			val trig=tran.trigger
			if (trig instanceof EventTrigger) {
				val er=trig.eventReference
				if (er instanceof PortEventReference){
					/*
					 * Gamma interface references are unresolved in the standalone ResourceSet.
					 * The token is retrieved directly from the parsed syntax tree.
					 */
					val node=NodeModelUtils.findNodesForFeature(er, StatechartModelPackage.Literals.PORT_EVENT_REFERENCE__EVENT)
					result.add(NodeModelUtils.getTokenText(node.first))
				}
			}
		}
		return result;
	}
	
	def static createModelicaCode(SynchronousStatechartDefinition model, String packageName)'''
package «packageName»

class «model.name»
import Modelica.StateGraph.InitialStep;
import Modelica.StateGraph.Step;
import Modelica.StateGraph.Transition;

«/*TODO:redo this part */»
«FOR port : model.ports»
	Modelica.Blocks.Interfaces.«IF port.isRequired»BooleanInput«ELSE»BooleanOutput«ENDIF» «port.name.toFirstLower»;
«ENDFOR»



end «model.name»;

end «packageName»;
'''
	
	def static void printTree(EObject object, String indent) {
		println(indent+object.eClass.name)
	    
	    for (child : object.eContents) {
	        printTree(child, indent + "  ")
	    }
	}
}

