package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.expression.model.Expression;
import hu.bme.mit.gamma.expression.model.FunctionDeclaration;
import hu.bme.mit.gamma.expression.model.ParameterDeclaration;
import hu.bme.mit.gamma.expression.model.VariableDeclaration;
import hu.bme.mit.gamma.statechart.interface_.Component;
import hu.bme.mit.gamma.statechart.interface_.ComponentAnnotation;
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage;
import hu.bme.mit.gamma.statechart.interface_.Port;
import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup;
import hu.bme.mit.gamma.statechart.statechart.GuardEvaluation;
import hu.bme.mit.gamma.statechart.statechart.OrthogonalRegionSchedulingOrder;
import hu.bme.mit.gamma.statechart.statechart.Region;
import hu.bme.mit.gamma.statechart.statechart.SchedulingOrder;
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition;
import hu.bme.mit.gamma.statechart.statechart.TimeoutDeclaration;
import hu.bme.mit.gamma.statechart.statechart.Transition;
import hu.bme.mit.gamma.statechart.statechart.TransitionPriority;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtend2.lib.StringConcatenation;
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
        final CharSequence code = Main.createModelicaCode(((SynchronousStatechartDefinition)statechart), pname);
        InputOutput.<CharSequence>print(code);
        InputOutput.<EList<ComponentAnnotation>>println(((SynchronousStatechartDefinition)statechart).getAnnotations());
        InputOutput.<EList<FunctionDeclaration>>println(((SynchronousStatechartDefinition)statechart).getFunctionDeclarations());
        InputOutput.<GuardEvaluation>println(((SynchronousStatechartDefinition)statechart).getGuardEvaluation());
        InputOutput.<EList<Expression>>println(((SynchronousStatechartDefinition)statechart).getInvariants());
        InputOutput.<OrthogonalRegionSchedulingOrder>println(((SynchronousStatechartDefinition)statechart).getOrthogonalRegionSchedulingOrder());
        InputOutput.<EList<ParameterDeclaration>>println(((SynchronousStatechartDefinition)statechart).getParameterDeclarations());
        InputOutput.<EList<Port>>println(((SynchronousStatechartDefinition)statechart).getPorts());
        InputOutput.<EList<Region>>println(((SynchronousStatechartDefinition)statechart).getRegions());
        InputOutput.<SchedulingOrder>println(((SynchronousStatechartDefinition)statechart).getSchedulingOrder());
        InputOutput.<EList<TimeoutDeclaration>>println(((SynchronousStatechartDefinition)statechart).getTimeoutDeclarations());
        InputOutput.<TransitionPriority>println(((SynchronousStatechartDefinition)statechart).getTransitionPriority());
        InputOutput.<EList<Transition>>println(((SynchronousStatechartDefinition)statechart).getTransitions());
        InputOutput.<EList<VariableDeclaration>>println(((SynchronousStatechartDefinition)statechart).getVariableDeclarations());
      } else {
        InputOutput.<String>println("Only synchronous statecharts are supported");
      }
    } else {
      InputOutput.<String>println("Unexpected root. Expected type: Package");
    }
    Main.printTree(root, "");
  }

  public static CharSequence createModelicaCode(final SynchronousStatechartDefinition model, final String packageName) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    _builder.append(packageName);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("class ");
    String _name = model.getName();
    _builder.append(_name);
    _builder.newLineIfNotEmpty();
    _builder.append("import Modelica.StateGraph.InitialStep;");
    _builder.newLine();
    _builder.append("import Modelica.StateGraph.Step;");
    _builder.newLine();
    _builder.append("import Modelica.StateGraph.Transition;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("end ");
    String _name_1 = model.getName();
    _builder.append(_name_1);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("end ");
    _builder.append(packageName);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    return _builder;
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
