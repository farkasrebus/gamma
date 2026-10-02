package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.action.model.Action;
import hu.bme.mit.gamma.statechart.interface_.Component;
import hu.bme.mit.gamma.statechart.interface_.EventReference;
import hu.bme.mit.gamma.statechart.interface_.EventTrigger;
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelPackage;
import hu.bme.mit.gamma.statechart.interface_.Port;
import hu.bme.mit.gamma.statechart.interface_.RealizationMode;
import hu.bme.mit.gamma.statechart.interface_.Trigger;
import hu.bme.mit.gamma.statechart.language.StatechartLanguageStandaloneSetup;
import hu.bme.mit.gamma.statechart.statechart.PortEventReference;
import hu.bme.mit.gamma.statechart.statechart.RaiseEventAction;
import hu.bme.mit.gamma.statechart.statechart.Region;
import hu.bme.mit.gamma.statechart.statechart.State;
import hu.bme.mit.gamma.statechart.statechart.StateNode;
import hu.bme.mit.gamma.statechart.statechart.StatechartModelPackage;
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition;
import hu.bme.mit.gamma.statechart.statechart.Transition;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.StringExtensions;

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
        final CharSequence code = Main.createModelicaCode(((SynchronousStatechartDefinition)statechart), pname);
        InputOutput.<CharSequence>print(code);
        EList<Region> _regions = ((SynchronousStatechartDefinition)statechart).getRegions();
        for (final Region r : _regions) {
          {
            final HashSet<String> raisedEvents = new HashSet<String>();
            Main.getEntryEvents(r, raisedEvents);
            InputOutput.<HashSet<String>>print(raisedEvents);
          }
        }
      } else {
        InputOutput.<String>println("Only synchronous statecharts are supported");
      }
    } else {
      InputOutput.<String>println("Unexpected root. Expected type: Package");
    }
    Main.printTree(root, "");
  }

  public static void getEntryEvents(final Region r, final Set<String> result) {
    EList<StateNode> _stateNodes = r.getStateNodes();
    for (final StateNode sn : _stateNodes) {
      {
        InputOutput.<StateNode>println(sn);
        if ((sn instanceof State)) {
          EList<Action> _entryActions = ((State)sn).getEntryActions();
          for (final Action a : _entryActions) {
            if ((a instanceof RaiseEventAction)) {
              final List<INode> node = NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT);
              String _name = ((RaiseEventAction)a).getPort().getName();
              String _plus = (_name + "_");
              String _tokenText = NodeModelUtils.getTokenText(node.getFirst());
              String _plus_1 = (_plus + _tokenText);
              result.add(_plus_1);
            }
          }
          EList<Region> _regions = ((State)sn).getRegions();
          for (final Region ir : _regions) {
            Main.getEntryEvents(ir, result);
          }
        }
      }
    }
  }

  public static Object collectEvent(final Action a, final Set<String> result) {
    return null;
  }

  public static boolean isRequired(final Port p) {
    RealizationMode _realizationMode = p.getInterfaceRealization().getRealizationMode();
    return Objects.equals(_realizationMode, RealizationMode.REQUIRED);
  }

  public static HashSet<String> getTriggerEvents(final SynchronousStatechartDefinition sct) {
    final HashSet<String> result = new HashSet<String>();
    EList<Transition> _transitions = sct.getTransitions();
    for (final Transition tran : _transitions) {
      {
        final Trigger trig = tran.getTrigger();
        if ((trig instanceof EventTrigger)) {
          final EventReference er = ((EventTrigger)trig).getEventReference();
          if ((er instanceof PortEventReference)) {
            final List<INode> node = NodeModelUtils.findNodesForFeature(er, StatechartModelPackage.Literals.PORT_EVENT_REFERENCE__EVENT);
            result.add(NodeModelUtils.getTokenText(node.getFirst()));
          }
        }
      }
    }
    return result;
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
    _builder.newLine();
    {
      EList<Port> _ports = model.getPorts();
      for(final Port port : _ports) {
        _builder.append("Modelica.Blocks.Interfaces.");
        {
          boolean _isRequired = Main.isRequired(port);
          if (_isRequired) {
            _builder.append("BooleanInput");
          } else {
            _builder.append("BooleanOutput");
          }
        }
        _builder.append(" ");
        String _firstLower = StringExtensions.toFirstLower(port.getName());
        _builder.append(_firstLower);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
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
