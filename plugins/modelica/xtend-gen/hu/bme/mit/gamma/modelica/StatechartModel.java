package hu.bme.mit.gamma.modelica;

import hu.bme.mit.gamma.action.model.Action;
import hu.bme.mit.gamma.statechart.interface_.EventReference;
import hu.bme.mit.gamma.statechart.interface_.EventTrigger;
import hu.bme.mit.gamma.statechart.interface_.Port;
import hu.bme.mit.gamma.statechart.interface_.Trigger;
import hu.bme.mit.gamma.statechart.statechart.EntryState;
import hu.bme.mit.gamma.statechart.statechart.PortEventReference;
import hu.bme.mit.gamma.statechart.statechart.RaiseEventAction;
import hu.bme.mit.gamma.statechart.statechart.Region;
import hu.bme.mit.gamma.statechart.statechart.State;
import hu.bme.mit.gamma.statechart.statechart.StateNode;
import hu.bme.mit.gamma.statechart.statechart.StatechartModelPackage;
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition;
import hu.bme.mit.gamma.statechart.statechart.Transition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class StatechartModel {
  public SynchronousStatechartDefinition model;

  public Map<String, Port> triggerEvents = new HashMap<String, Port>();

  public Map<String, Port> raisedEvents = new HashMap<String, Port>();

  public Map<Region, EntryState> entries = new HashMap<Region, EntryState>();

  public Map<Region, Set<State>> normalStates = new HashMap<Region, Set<State>>();

  public Map<Region, Set<State>> compositeStates = new HashMap<Region, Set<State>>();

  public Map<Region, Set<Transition>> transitions = new HashMap<Region, Set<Transition>>();

  public Map<StateNode, Set<Transition>> incoming = new HashMap<StateNode, Set<Transition>>();

  public Map<StateNode, Set<Transition>> outgoing = new HashMap<StateNode, Set<Transition>>();

  public StatechartModel(final SynchronousStatechartDefinition ssd) {
    this.model = ssd;
    EList<Region> _regions = this.model.getRegions();
    for (final Region r : _regions) {
      {
        this.findEvents(r);
        this.findStates(r);
      }
    }
    this.findTriggerEvents();
    this.handleTransitions();
  }

  public void handleTransitions() {
    EList<Transition> _transitions = this.model.getTransitions();
    for (final Transition t : _transitions) {
      {
        final StateNode src = t.getSourceState();
        EObject _eContainer = src.eContainer();
        final Region r = ((Region) _eContainer);
        boolean _containsKey = this.transitions.containsKey(r);
        boolean _not = (!_containsKey);
        if (_not) {
          HashSet<Transition> _hashSet = new HashSet<Transition>();
          this.transitions.put(r, _hashSet);
        }
        this.transitions.get(r).add(t);
        boolean _containsKey_1 = this.outgoing.containsKey(src);
        boolean _not_1 = (!_containsKey_1);
        if (_not_1) {
          HashSet<Transition> _hashSet_1 = new HashSet<Transition>();
          this.outgoing.put(src, _hashSet_1);
        }
        this.outgoing.get(src).add(t);
        final StateNode trg = t.getTargetState();
        boolean _containsKey_2 = this.incoming.containsKey(trg);
        boolean _not_2 = (!_containsKey_2);
        if (_not_2) {
          HashSet<Transition> _hashSet_2 = new HashSet<Transition>();
          this.incoming.put(trg, _hashSet_2);
        }
        this.incoming.get(trg).add(t);
      }
    }
  }

  public void findStates(final Region r) {
    final HashSet<State> normals = new HashSet<State>();
    this.normalStates.put(r, normals);
    final HashSet<State> comp = new HashSet<State>();
    this.compositeStates.put(r, comp);
    EList<StateNode> _stateNodes = r.getStateNodes();
    for (final StateNode sn : _stateNodes) {
      if ((sn instanceof EntryState)) {
        this.entries.put(r, ((EntryState)sn));
      } else {
        if ((sn instanceof State)) {
          int _size = ((State)sn).getRegions().size();
          boolean _greaterThan = (_size > 0);
          if (_greaterThan) {
            comp.add(((State)sn));
            EList<Region> _regions = ((State)sn).getRegions();
            for (final Region ir : _regions) {
              this.findStates(ir);
            }
          } else {
            normals.add(((State)sn));
          }
        }
      }
    }
  }

  public void findEvents(final Region r) {
    EList<StateNode> _stateNodes = r.getStateNodes();
    for (final StateNode sn : _stateNodes) {
      if ((sn instanceof State)) {
        EList<Action> _entryActions = ((State)sn).getEntryActions();
        for (final Action a : _entryActions) {
          if ((a instanceof RaiseEventAction)) {
            final List<INode> node = NodeModelUtils.findNodesForFeature(a, StatechartModelPackage.Literals.RAISE_EVENT_ACTION__EVENT);
            this.raisedEvents.put(NodeModelUtils.getTokenText(node.getFirst()), ((RaiseEventAction)a).getPort());
          }
        }
        EList<Region> _regions = ((State)sn).getRegions();
        for (final Region ir : _regions) {
          this.findEvents(ir);
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
            this.triggerEvents.put(NodeModelUtils.getTokenText(node.getFirst()), ((PortEventReference)er).getPort());
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
        String _firstLower = StringExtensions.toFirstLower(this.triggerEvents.get(e).getName());
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
        String _firstLower_1 = StringExtensions.toFirstLower(this.raisedEvents.get(e_1).getName());
        _builder.append(_firstLower_1);
        _builder.append("_");
        _builder.append(e_1);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("InitialStep ");
    String _firstLower_2 = StringExtensions.toFirstLower(this.entries.get(this.model.getRegions().get(0)).getName());
    _builder.append(_firstLower_2);
    _builder.append("(nIn=0, nOut=1);");
    _builder.newLineIfNotEmpty();
    {
      Set<State> _get = this.compositeStates.get(this.model.getRegions().get(0));
      for(final State s : _get) {
        String _name_1 = this.model.getName();
        _builder.append(_name_1);
        String _firstUpper = StringExtensions.toFirstUpper(s.getName());
        _builder.append(_firstUpper);
        _builder.append(" ");
        String _firstLower_3 = StringExtensions.toFirstLower(s.getName());
        _builder.append(_firstLower_3);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.append("Step ");
        String _firstLower_4 = StringExtensions.toFirstLower(s.getName());
        _builder.append(_firstLower_4);
        _builder.append("Entry(nIn=");
        {
          boolean _containsKey = this.incoming.containsKey(s);
          if (_containsKey) {
            int _size = this.incoming.get(s).size();
            _builder.append(_size);
          } else {
            _builder.append("0");
          }
        }
        _builder.append(",nOut=1);");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      Set<State> _get_1 = this.normalStates.get(this.model.getRegions().get(0));
      for(final State s_1 : _get_1) {
        _builder.append("Step ");
        String _firstLower_5 = StringExtensions.toFirstLower(s_1.getName());
        _builder.append(_firstLower_5);
        _builder.append("(nIn=");
        {
          boolean _containsKey_1 = this.incoming.containsKey(s_1);
          if (_containsKey_1) {
            int _size_1 = this.incoming.get(s_1).size();
            _builder.append(_size_1);
          } else {
            _builder.append("0");
          }
        }
        _builder.append(",nOut=");
        {
          boolean _containsKey_2 = this.outgoing.containsKey(s_1);
          if (_containsKey_2) {
            int _size_2 = this.outgoing.get(s_1).size();
            _builder.append(_size_2);
          } else {
            _builder.append("0");
          }
        }
        _builder.append(");");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("end ");
    String _name_2 = this.model.getName();
    _builder.append(_name_2);
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
