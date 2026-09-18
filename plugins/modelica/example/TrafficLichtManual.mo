model TrafficLichtManual
import Modelica.StateGraph.InitialStep;
import Modelica.StateGraph.Step;
import Modelica.StateGraph.Transition;

Modelica.Blocks.Interfaces.BooleanOutput controlToggle;
Modelica.Blocks.Interfaces.BooleanOutput greenLamp;

 InitialStep initialStep(nIn=0, nOut=1);//Just a pseudostate,representing an entry state
 TrafficLightNormalManual normal;
 Step preNormal(nIn=2,nOut=1); //Created to "join" all incoming transitions to the composite step
 TrafficLightInterruptedManual interrupted;
 
 Transition t1(enableTimer=true, waitTime=0);//Wait time 0, because the initialstep is just a transient state
 Transition t2(enableTimer=true, waitTime=0);//Wait time 0, because preNormal is also just a transient state
 
 
 Transition t3(enableTimer=true, waitTime=2);//Leaving enough time for the internal steps to become active
 Transition t4(enableTimer=true, waitTime=1);
 
 initial equation
 controlToggle=false;
 greenLamp=false;

equation
  connect(initialStep.outPort[1], t1.inPort);
  connect(t1.outPort,preNormal.inPort[1]);//all incoming transitions point to the "join" state
  connect(preNormal.outPort[1],t2.inPort);
  connect(t2.outPort,normal.inPort);
  connect(normal.suspend[1],t3.inPort);
  connect(t3.outPort,interrupted.inPort);
  connect(interrupted.suspend[1],t4.inPort);
  connect(t4.outPort,preNormal.inPort[2]);
  
  when time == 1.0 then
    controlToggle=true;
  elsewhen pre(controlToggle) then
    controlToggle = false;
  end when;
  
  when edge(normal.displayGreen) then
    greenLamp = true;
  end when;
 

  normal.toggle=controlToggle;
  
annotation(
    experiment(StartTime = 0, StopTime = 10, Tolerance = 1e-06, Interval = 0.02)); //This line is there so simulation doesn't have to be set every time
end TrafficLichtManual;
