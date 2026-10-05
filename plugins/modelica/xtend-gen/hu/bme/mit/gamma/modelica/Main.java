package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.action.model.Action;
import hu.bme.mit.gamma.statechart.interface_.Component;
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage;
import hu.bme.mit.gamma.statechart.interface_.Port;
import hu.bme.mit.gamma.statechart.interface_.RealizationMode;
import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup;
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
    EcoreUtil.resolveAll(resourceSet);
    int _size = resource.getContents().size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      InputOutput.<String>println("Only one root is supported.");
    }
    final EObject root = resource.getContents().getFirst();
    if ((root instanceof hu.bme.mit.gamma.statechart.interface_.Package)) {
      final String pname = ((hu.bme.mit.gamma.statechart.interface_.Package)root).getName();
      int _size_1 = ((hu.bme.mit.gamma.statechart.interface_.Package)root).getComponents().size();
      boolean _notEquals = (_size_1 != 1);
      if (_notEquals) {
        InputOutput.<String>println("Only one component (Statechart) is supported");
      }
      final Component statechart = ((hu.bme.mit.gamma.statechart.interface_.Package)root).getComponents().getFirst();
      if ((statechart instanceof SynchronousStatechartDefinition)) {
        final StatechartModel model = new StatechartModel(((SynchronousStatechartDefinition)statechart));
        final CharSequence code = model.createModelicaCode(pname);
        InputOutput.<CharSequence>print(code);
      } else {
        InputOutput.<String>println("Only synchronous statecharts are supported");
      }
    } else {
      InputOutput.<String>println("Unexpected root. Expected type: Package");
    }
    Main.printTree(root, "");
  }

  public static Object collectEvent(final Action a, final Set<String> result) {
    return null;
  }

  public static boolean isRequired(final Port p) {
    RealizationMode _realizationMode = p.getInterfaceRealization().getRealizationMode();
    return Objects.equals(_realizationMode, RealizationMode.REQUIRED);
  }

  public static void printTree(final EObject object, final String indent) {
    String _name = object.eClass().getName();
    String _plus = (indent + _name);
    InputOutput.<String>println(_plus);
    EList<EObject> _eContents = object.eContents();
    for (final EObject child : _eContents) {
      Main.printTree(child, (indent + "  "));
    }
  }
}
