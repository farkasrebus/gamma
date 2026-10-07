model Example

Boolean toggle;
TrafficLichtManual tlm;

initial equation
  toggle=false;

equation
  tlm.controlToggle=toggle;

  when time == 1.0 then
    toggle=true;
  elsewhen pre(toggle) then
    toggle = false;
  end when;

annotation(
    experiment(StartTime = 0, StopTime = 10, Tolerance = 1e-06, Interval = 0.02));
end Example;
