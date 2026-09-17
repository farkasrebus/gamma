class TrafficLightInterruptedManual
  extends Modelica.StateGraph.PartialCompositeStep;
  import Modelica.StateGraph.Step;
  import Modelica.StateGraph.Transition;
  
  Step blinkingYellow(nIn=2, nOut=1);
  Step black(nIn=1, nOut=1);
  
  Transition t1(enableTimer=true, waitTime=1);
  Transition t2(enableTimer=true, waitTime=2);
equation
  connect(blinkingYellow.inPort[1],inPort);
  connect(blinkingYellow.outPort[1],t1.inPort);
  connect(t1.outPort,black.inPort[1]);
  connect(black.outPort[1],t2.inPort);
  connect(t2.outPort,blinkingYellow.inPort[2]);
  
end TrafficLightInterruptedManual;
