package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage;
import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.xbase.lib.InputOutput;

@SuppressWarnings("all")
public class Main {
  public static void main(final String[] args) {
    StatechartLanguageStandaloneSetup.doSetup();
    final InterfaceModelPackage package_ = InterfaceModelPackage.eINSTANCE;
    final ResourceSetImpl resourceSet = new ResourceSetImpl();
    final Resource resource = resourceSet.getResource(
      URI.createFileURI("D:/git/gamma/tutorial/hu.bme.mit.gamma.tutorial.finish/model/TrafficLight/TrafficLightCtrl.gcd"), 
      true);
    URI _uRI = resource.getURI();
    String _plus = ("Loaded resource: " + _uRI);
    InputOutput.<String>println(_plus);
    int _size = resource.getContents().size();
    String _plus_1 = ("Root objects: " + Integer.valueOf(_size));
    InputOutput.<String>println(_plus_1);
    EList<EObject> _contents = resource.getContents();
    for (final EObject root : _contents) {
      {
        String _name = root.eClass().getName();
        String _plus_2 = ("Root type: " + _name);
        InputOutput.<String>println(_plus_2);
        EList<EObject> _eContents = root.eContents();
        for (final EObject child : _eContents) {
          String _name_1 = child.eClass().getName();
          String _plus_3 = ("  " + _name_1);
          InputOutput.<String>println(_plus_3);
        }
      }
    }
  }
}
