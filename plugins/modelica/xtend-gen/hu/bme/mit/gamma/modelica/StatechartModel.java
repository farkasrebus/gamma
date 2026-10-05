package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.action.model.Action;
import hu.bme.mit.gamma.statechart.interface_.EventReference;
import hu.bme.mit.gamma.statechart.interface_.EventTrigger;
import hu.bme.mit.gamma.statechart.interface_.Trigger;
import hu.bme.mit.gamma.statechart.statechart.PortEventReference;
import hu.bme.mit.gamma.statechart.statechart.RaiseEventAction;
import hu.bme.mit.gamma.statechart.statechart.Region;
import hu.bme.mit.gamma.statechart.statechart.State;
import hu.bme.mit.gamma.statechart.statechart.StateNode;
import hu.bme.mit.gamma.statechart.statechart.StatechartModelPackage;
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition;
import hu.bme.mit.gamma.statechart.statechart.Transition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class StatechartModel {
  public SynchronousStatechartDefinition model;

  public Map<String, String> triggerEvents = new HashMap<String, String>();

  public Map<String, String> raisedEvents = new HashMap<String, String>();

  public StatechartModel(final SynchronousStatechartDefinition ssd) {
    this.model = ssd;
    EList<Region> _regions = this.model.getRegions();
    for (final Region r : _regions) {
      this.findEntryEvents(r);
    }
    this.findTriggerEvents();
  }

  public void findEntryEvents(final Region r) {
    EList<StateNode> _stateNodes = r.getStateNodes();
    for (final StateNode sn : _stateNodes) {
      if ((sn instanceof State)) {
        EList<Action> _entryActions = ((State)sn).getEntryActions();
        for (final Action a : _entryActions) {
          if ((a instanceof RaiseEventAction)) {
            final List<INode> node = NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT);
            this.raisedEvents.put(NodeModelUtils.getTokenText(node.getFirst()), ((RaiseEventAction)a).getPort().getName());
          }
        }
        EList<Region> _regions = ((State)sn).getRegions();
        for (final Region ir : _regions) {
          this.findEntryEvents(ir);
        }
      }
    }
  }

  public void findTriggerEvents() {
    EList<Transition> _transitions = this.model.getTransitions();
    for (final Transition tran : _transitions) {
      {
        final Trigger trig = tran.getTrigger();
        if ((trig instanceof EventTrigger)) {
          final EventReference er = ((EventTrigger)trig).getEventReference();
          if ((er instanceof PortEventReference)) {
            final List<INode> node = NodeModelUtils.findNodesForFeature(er, StatechartModelPackage.Literals.PORT_EVENT_REFERENCE__EVENT);
            this.triggerEvents.put(NodeModelUtils.getTokenText(node.getFirst()), ((PortEventReference)er).getPort().getName());
          }
        }
      }
    }
  }

  public CharSequence createModelicaCode(final String packageName) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    _builder.append(packageName);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("class ");
    String _name = this.model.getName();
    _builder.append(_name);
    _builder.newLineIfNotEmpty();
    _builder.append("import Modelica.StateGraph.InitialStep;");
    _builder.newLine();
    _builder.append("import Modelica.StateGraph.Step;");
    _builder.newLine();
    _builder.append("import Modelica.StateGraph.Transition;");
    _builder.newLine();
    _builder.newLine();
    {
      Set<String> _keySet = this.triggerEvents.keySet();
      for(final String e : _keySet) {
        _builder.append("Modelica.Blocks.Interfaces.BooleanInput ");
        String _firstLower = StringExtensions.toFirstLower(this.triggerEvents.get(e));
        _builder.append(_firstLower);
        _builder.append("_");
        _builder.append(e);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      Set<String> _keySet_1 = this.raisedEvents.keySet();
      for(final String e_1 : _keySet_1) {
        _builder.append("Modelica.Blocks.Interfaces.BooleanOutput ");
        String _firstLower_1 = StringExtensions.toFirstLower(this.raisedEvents.get(e_1));
        _builder.append(_firstLower_1);
        _builder.append("_");
        _builder.append(e_1);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.newLine();
    _builder.append("end ");
    String _name_1 = this.model.getName();
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
}
