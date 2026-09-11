package hu.bme.mit.gamma.modelica

import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.common.util.URI
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage

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
        println("Loaded resource: " + resource.URI)
        println("Root objects: " + resource.contents.size)

        /*for (root : resource.contents) {
            println("Root type: " + root.eClass.name)
        }*/
        for (root : resource.contents) {
		    println("Root type: " + root.eClass.name)
		
		    for (child : root.eContents) {
		        println("  " + child.eClass.name)
		    }
		}
		
	}
}