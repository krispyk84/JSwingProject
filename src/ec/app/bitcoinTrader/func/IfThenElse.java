/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.bitcoinTrader.func;
import ec.*;
import ec.app.bitcoinTrader.*;
import ec.gp.*;
import ec.util.*;

public class IfThenElse extends GPNode
    {
    public String toString() { return "IfThenElse"; }

    public int expectedChildren() { return 3; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double result, if_arg, then_arg, else_arg;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        if_arg = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        then_arg = rd.x;

      children[2].eval(state,thread,input,stack,individual,problem);
        else_arg = rd.x;

      if (if_arg >= 1){
       result = then_arg;
      } else {
       result = else_arg;
      }
      rd.x = result;
        }
    }

