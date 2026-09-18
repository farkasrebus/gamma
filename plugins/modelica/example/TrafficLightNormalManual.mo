class TrafficLightNormalManual
  extends Modelica.StateGraph.PartialCompositeStep;
  import Modelica.StateGraph.Step;
  import Modelica.StateGraph.Transition;
  
  Modelica.Blocks.Interfaces.BooleanInput toggle;
  Modelica.Blocks.Interfaces.BooleanOutput displayGreen;
  
  Step red(nIn=2, nOut=1); //Now with real states :)
  Step green(nIn=1, nOut=1);
  Step yellow(nIn=1, nOut=1);
  
  Transition t1(condition=toggle);
  Transition t2(enableTimer=true, waitTime=2); //Time set to 2, so green is still the active state when the state machine is interrupted.
  Transition t3(enableTimer=true, waitTime=1);
 equation
  
  connect(red.inPort[1],inPort);
  connect(red.outPort[1], t1.inPort);
  connect(t1.outPort,green.inPort[1]);
  connect(green.outPort[1],t2.inPort);
  connect(t2.outPort,yellow.inPort[1]);
  connect(yellow.outPort[1],t3.inPort);
  connect(t3.outPort,red.inPort[2]);
  
  when t1.fire then
    displayGreen=true;
  elsewhen pre(displayGreen) then
    displayGreen = false;
  end when;
  
  
annotation(
    experiment(StartTime = 0, StopTime = 1, Tolerance = 1e-06, Interval = 0.002));
end TrafficLightNormalManual;
