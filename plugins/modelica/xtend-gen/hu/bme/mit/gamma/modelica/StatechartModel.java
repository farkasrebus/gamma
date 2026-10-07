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
import hu.bme.mit.gamma.statechart.statechart.TimeoutEventReference;
import hu.bme.mit.gamma.statechart.statechart.Transition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class StatechartModel {
  public SynchronousStatechartDefinition model;

  public Map<String, Port> eventPorts = new HashMap<String, Port>();

  public Map<Transition, String> triggerEvents = new HashMap<Transition, String>();

  public Map<State, String> raisedEvents = new HashMap<State, String>();

  public Map<Region, EntryState> entries = new HashMap<Region, EntryState>();

  public Map<Region, Set<State>> normalStates = new HashMap<Region, Set<State>>();

  public Map<Region, Set<State>> compositeStates = new HashMap<Region, Set<State>>();

  public Map<Region, Set<Transition>> transitions = new HashMap<Region, Set<Transition>>();

  public Map<StateNode, List<Transition>> incoming = new HashMap<StateNode, List<Transition>>();

  public Map<StateNode, List<Transition>> outgoing = new HashMap<StateNode, List<Transition>>();

  private final HashMap<Transition, String> transitionNames = new HashMap<Transition, String>();

  private int transitionCounter = 0;

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
          ArrayList<Transition> _arrayList = new ArrayList<Transition>();
          this.outgoing.put(src, _arrayList);
        }
        this.outgoing.get(src).add(t);
        final StateNode trg = t.getTargetState();
        boolean _containsKey_2 = this.incoming.containsKey(trg);
        boolean _not_2 = (!_containsKey_2);
        if (_not_2) {
          ArrayList<Transition> _arrayList_1 = new ArrayList<Transition>();
          this.incoming.put(trg, _arrayList_1);
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
            final String event = NodeModelUtils.getTokenText(node.getFirst());
            this.eventPorts.put(event, ((RaiseEventAction)a).getPort());
            this.raisedEvents.put(((State)sn), event);
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
            final String event = NodeModelUtils.getTokenText(node.getFirst());
            this.eventPorts.put(event, ((PortEventReference)er).getPort());
            this.triggerEvents.put(tran, event);
          }
        }
      }
    }
  }

  public String getTransitionName(final Transition transition) {
    final Function<Transition, String> _function = (Transition it) -> {
      String _xblockexpression = null;
      {
        this.transitionCounter++;
        _xblockexpression = ("t" + Integer.valueOf(this.transitionCounter));
      }
      return _xblockexpression;
    };
    return this.transitionNames.computeIfAbsent(transition, _function);
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
    _builder.append("  ");
    _builder.append("//implementing events as boolean impulses");
    _builder.newLine();
    {
      Set<String> _set = IterableExtensions.<String>toSet(this.triggerEvents.values());
      for(final String e : _set) {
        _builder.append("  ");
        _builder.append("Modelica.Blocks.Interfaces.BooleanInput ");
        String _modelicaName = this.getModelicaName(e);
        _builder.append(_modelicaName, "  ");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.append("//internal events for raised events");
    _builder.newLine();
    {
      Set<String> _set_1 = IterableExtensions.<String>toSet(this.raisedEvents.values());
      for(final String e_1 : _set_1) {
        _builder.append("  ");
        _builder.append("Modelica.Blocks.Interfaces.BooleanOutput ");
        String _modelicaName_1 = this.getModelicaName(e_1);
        _builder.append(_modelicaName_1, "  ");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.append("  ");
        _builder.append("Boolean ");
        String _modelicaName_2 = this.getModelicaName(e_1);
        _builder.append(_modelicaName_2, "  ");
        _builder.append("Internal;");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//The main entry is translated to an intital step");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("InitialStep ");
    String _firstLower = StringExtensions.toFirstLower(this.entries.get(this.model.getRegions().get(0)).getName());
    _builder.append(_firstLower, "  ");
    _builder.append("(nIn=0, nOut=1);");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    _builder.append("//Composite states instantiated");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Additional \"_Entry\" states join all incoming transitions");
    _builder.newLine();
    {
      Set<State> _get = this.compositeStates.get(this.model.getRegions().get(0));
      for(final State s : _get) {
        _builder.append("  ");
        String _name_1 = this.model.getName();
        _builder.append(_name_1, "  ");
        String _firstUpper = StringExtensions.toFirstUpper(s.getName());
        _builder.append(_firstUpper, "  ");
        _builder.append(" ");
        String _firstLower_1 = StringExtensions.toFirstLower(s.getName());
        _builder.append(_firstLower_1, "  ");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.append("  ");
        _builder.append("Step ");
        String _firstLower_2 = StringExtensions.toFirstLower(s.getName());
        _builder.append(_firstLower_2, "  ");
        _builder.append("Entry(nIn=");
        {
          boolean _containsKey = this.incoming.containsKey(s);
          if (_containsKey) {
            int _size = this.incoming.get(s).size();
            _builder.append(_size, "  ");
          } else {
            _builder.append("0");
          }
        }
        _builder.append(",nOut=1);");
        _builder.newLineIfNotEmpty();
        _builder.append("  ");
        _builder.append("Transition entryTo");
        String _firstUpper_1 = StringExtensions.toFirstUpper(s.getName());
        _builder.append(_firstUpper_1, "  ");
        _builder.append("(enableTimer=true, waitTime=0);");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.append("//Instantiating regular not composite) states");
    _builder.newLine();
    {
      Set<State> _get_1 = this.normalStates.get(this.model.getRegions().get(0));
      for(final State s_1 : _get_1) {
        _builder.append("  ");
        _builder.append("Step ");
        String _firstLower_3 = StringExtensions.toFirstLower(s_1.getName());
        _builder.append(_firstLower_3, "  ");
        _builder.append("(nIn=");
        {
          boolean _containsKey_1 = this.incoming.containsKey(s_1);
          if (_containsKey_1) {
            int _size_1 = this.incoming.get(s_1).size();
            _builder.append(_size_1, "  ");
          } else {
            _builder.append("0");
          }
        }
        _builder.append(",nOut=");
        {
          boolean _containsKey_2 = this.outgoing.containsKey(s_1);
          if (_containsKey_2) {
            int _size_2 = this.outgoing.get(s_1).size();
            _builder.append(_size_2, "  ");
          } else {
            _builder.append("0");
          }
        }
        _builder.append(");");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.append("//Instatntiating transitions, depending on condition");
    _builder.newLine();
    {
      Set<Transition> _get_2 = this.transitions.get(this.model.getRegions().get(0));
      for(final Transition t : _get_2) {
        _builder.append("  ");
        _builder.append("Transition ");
        String _transitionName = this.getTransitionName(t);
        _builder.append(_transitionName, "  ");
        _builder.append(" ");
        {
          boolean _isDelayed = this.isDelayed(t);
          if (_isDelayed) {
            _builder.append("(enableTimer=true, waitTime=");
            int _delay = this.getDelay(t);
            _builder.append(_delay, "  ");
          } else {
            _builder.append("(condition=");
            String _modelicaName_3 = this.getModelicaName(this.triggerEvents.get(t));
            _builder.append(_modelicaName_3, "  ");
          }
        }
        _builder.append(");");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("  ");
    _builder.append("initial equation");
    _builder.newLine();
    {
      Set<String> _set_2 = IterableExtensions.<String>toSet(this.raisedEvents.values());
      for(final String e_2 : _set_2) {
        _builder.append("  ");
        String _modelicaName_4 = this.getModelicaName(e_2);
        _builder.append(_modelicaName_4, "  ");
        _builder.append("Internal=false;");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("equation");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Connecting the additional _Entry states to composite states");
    _builder.newLine();
    {
      Set<State> _get_3 = this.compositeStates.get(this.model.getRegions().get(0));
      for(final State s_2 : _get_3) {
        _builder.append("  ");
        _builder.append("connect(");
        String _firstLower_4 = StringExtensions.toFirstLower(s_2.getName());
        _builder.append(_firstLower_4, "  ");
        _builder.append("Entry.outPort[1],entryTo");
        String _firstUpper_2 = StringExtensions.toFirstUpper(s_2.getName());
        _builder.append(_firstUpper_2, "  ");
        _builder.append(".inPort);");
        _builder.newLineIfNotEmpty();
        _builder.append("  ");
        _builder.append("connect(entryTo");
        String _firstUpper_3 = StringExtensions.toFirstUpper(s_2.getName());
        _builder.append(_firstUpper_3, "  ");
        _builder.append(".outPort,");
        String _firstLower_5 = StringExtensions.toFirstLower(s_2.getName());
        _builder.append(_firstLower_5, "  ");
        _builder.append(".inPort);");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Connecting original transitions");
    _builder.newLine();
    {
      Set<Transition> _get_4 = this.transitions.get(this.model.getRegions().get(0));
      for(final Transition t_1 : _get_4) {
        _builder.append("  ");
        _builder.append("connect(");
        String _firstLower_6 = StringExtensions.toFirstLower(t_1.getSourceState().getName());
        _builder.append(_firstLower_6, "  ");
        _builder.append(".");
        {
          boolean _isComposite = this.isComposite(t_1.getSourceState());
          if (_isComposite) {
            _builder.append("suspend");
          } else {
            _builder.append("outPort");
          }
        }
        _builder.append("[");
        int _indexOf = this.outgoing.get(t_1.getSourceState()).indexOf(t_1);
        int _plus = (_indexOf + 1);
        _builder.append(_plus, "  ");
        _builder.append("],");
        String _transitionName_1 = this.getTransitionName(t_1);
        _builder.append(_transitionName_1, "  ");
        _builder.append(".inPort);");
        _builder.newLineIfNotEmpty();
        _builder.append("  ");
        _builder.append("connect(");
        String _transitionName_2 = this.getTransitionName(t_1);
        _builder.append(_transitionName_2, "  ");
        _builder.append(".outPort,");
        String _firstLower_7 = StringExtensions.toFirstLower(t_1.getTargetState().getName());
        _builder.append(_firstLower_7, "  ");
        {
          boolean _isComposite_1 = this.isComposite(t_1.getTargetState());
          if (_isComposite_1) {
            _builder.append("Entry");
          }
        }
        _builder.append(".inPort[");
        int _indexOf_1 = this.incoming.get(t_1.getTargetState()).indexOf(t_1);
        int _plus_1 = (_indexOf_1 + 1);
        _builder.append(_plus_1, "  ");
        _builder.append("]);");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Connecting input variables to inputs for composite state classes");
    _builder.newLine();
    {
      Set<String> _set_3 = IterableExtensions.<String>toSet(this.triggerEvents.values());
      for(final String e_3 : _set_3) {
        {
          Set<State> _get_5 = this.compositeStates.get(this.model.getRegions().get(0));
          for(final State s_3 : _get_5) {
            _builder.append("  ");
            _builder.append("connect(");
            String _modelicaName_5 = this.getModelicaName(e_3);
            _builder.append(_modelicaName_5, "  ");
            _builder.append(",");
            String _firstLower_8 = StringExtensions.toFirstLower(s_3.getName());
            _builder.append(_firstLower_8, "  ");
            _builder.append(".");
            String _modelicaName_6 = this.getModelicaName(e_3);
            _builder.append(_modelicaName_6, "  ");
            _builder.append(");");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Connecting outputs: high-levels raise events by transitions or from lower level states");
    _builder.newLine();
    {
      Set<String> _set_4 = IterableExtensions.<String>toSet(this.raisedEvents.values());
      for(final String e_4 : _set_4) {
        _builder.append("  ");
        String _modelicaName_7 = this.getModelicaName(e_4);
        _builder.append(_modelicaName_7, "  ");
        _builder.append("=");
        String _modelicaName_8 = this.getModelicaName(e_4);
        _builder.append(_modelicaName_8, "  ");
        _builder.append("Internal");
        {
          Set<State> _get_6 = this.compositeStates.get(this.model.getRegions().get(0));
          for(final State s_4 : _get_6) {
            _builder.append(" or ");
            String _firstLower_9 = StringExtensions.toFirstLower(s_4.getName());
            _builder.append(_firstLower_9, "  ");
            _builder.append(".");
            String _modelicaName_9 = this.getModelicaName(e_4);
            _builder.append(_modelicaName_9, "  ");
          }
        }
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("//Raising high-level events");
    _builder.newLine();
    {
      Set<State> _get_7 = this.normalStates.get(this.model.getRegions().get(0));
      for(final State s_5 : _get_7) {
        _builder.append("  ");
        {
          boolean _containsKey_3 = this.raisedEvents.containsKey(s_5);
          if (_containsKey_3) {
            _builder.append("when ");
            String _firstLower_10 = StringExtensions.toFirstLower(s_5.getName());
            _builder.append(_firstLower_10, "  ");
            _builder.append(".active and not pre(");
            String _firstLower_11 = StringExtensions.toFirstLower(s_5.getName());
            _builder.append(_firstLower_11, "  ");
            _builder.append(".active) then");
            _builder.newLineIfNotEmpty();
            _builder.append("  ");
            _builder.append("  ");
            String _modelicaName_10 = this.getModelicaName(this.raisedEvents.get(s_5));
            _builder.append(_modelicaName_10, "    ");
            _builder.append("Internal=true;");
            _builder.newLineIfNotEmpty();
            _builder.append("  ");
            _builder.append("elsewhen pre(");
            String _modelicaName_11 = this.getModelicaName(this.raisedEvents.get(s_5));
            _builder.append(_modelicaName_11, "  ");
            _builder.append(") then");
            _builder.newLineIfNotEmpty();
            _builder.append("  ");
            _builder.append("  ");
            String _modelicaName_12 = this.getModelicaName(this.raisedEvents.get(s_5));
            _builder.append(_modelicaName_12, "    ");
            _builder.append("Internal=false;");
            _builder.newLineIfNotEmpty();
            _builder.append("  ");
            _builder.append("end when;");
            _builder.newLine();
          }
        }
      }
    }
    _builder.append("end ");
    String _name_2 = this.model.getName();
    _builder.append(_name_2);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.newLine();
    _builder.newLine();
    _builder.newLine();
    _builder.append("end ");
    _builder.append(packageName);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    return _builder;
  }

  public boolean isComposite(final StateNode state) {
    Collection<Set<State>> _values = this.compositeStates.values();
    for (final Set<State> c : _values) {
      boolean _contains = c.contains(state);
      if (_contains) {
        return true;
      }
    }
    return false;
  }

  /**
   * «»
   */
  public String getModelicaName(final String event) {
    String _firstLower = StringExtensions.toFirstLower(this.eventPorts.get(event).getName());
    String _plus = (_firstLower + "_");
    return (_plus + event);
  }

  public int getDelay(final Transition t) {
    StateNode _sourceState = t.getSourceState();
    if ((_sourceState instanceof EntryState)) {
      return 0;
    } else {
      Trigger _trigger = t.getTrigger();
      final EventTrigger et = ((EventTrigger) _trigger);
      EventReference _eventReference = et.getEventReference();
      final TimeoutEventReference to = ((TimeoutEventReference) _eventReference);
      InputOutput.<String>println(to.getTimeout().getName());
    }
    return 0;
  }

  public boolean isDelayed(final Transition t) {
    StateNode _sourceState = t.getSourceState();
    if ((_sourceState instanceof EntryState)) {
      return true;
    } else {
      final Trigger trig = t.getTrigger();
      if ((trig instanceof EventTrigger)) {
        EventReference _eventReference = ((EventTrigger)trig).getEventReference();
        if ((_eventReference instanceof TimeoutEventReference)) {
          return true;
        }
      }
    }
    return false;
  }
}
