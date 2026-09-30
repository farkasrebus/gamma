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
				for (Port port : statechart.ports) {
					val realization = port.interfaceRealization
				    val gammaInterface = realization.interface
				    
				    println(gammaInterface.eIsProxy)
					println(gammaInterface.name)
				    val internalInterface = gammaInterface as InternalEObject

					println(internalInterface.eIsProxy)
					println(internalInterface.eProxyURI)
				}
				for (tran: statechart.transitions) {
					val trig=tran.trigger
					if (trig instanceof EventTrigger) {
						val er=trig.eventReference
						if (er instanceof PortEventReference){
							val node=NodeModelUtils.findNodesForFeature(er, StatechartModelPackage.Literals.PORT_EVENT_REFERENCE__EVENT)
							println("..........."+NodeModelUtils.getTokenText(node.first))
							println(er.port.name+"."+er.event)
						}
					}
				}
				
				/*println(statechart.regions)//region
				println(statechart.schedulingOrder)//top down
				println(statechart.timeoutDeclarations)//timeouts!!!
				println(statechart.transitionPriority)//OFF
				println(statechart.transitions)//trans
				println(statechart.variableDeclarations)//*/
			} else println("Only synchronous statecharts are supported")
			
			
		} else println("Unexpected root. Expected type: Package")
		
		printTree(root,"")
	}
	
	def static isRequired(Port p) {
		return p.interfaceRealization.realizationMode == RealizationMode.REQUIRED;
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

