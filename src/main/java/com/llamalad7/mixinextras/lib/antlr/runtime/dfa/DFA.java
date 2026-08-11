package com.llamalad7.mixinextras.lib.antlr.runtime.dfa;

import com.llamalad7.mixinextras.lib.antlr.runtime.Vocabulary;
import com.llamalad7.mixinextras.lib.antlr.runtime.VocabularyImpl;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.ATNConfigSet;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.DecisionState;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.StarLoopEntryState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFA {
   public final Map<DFAState, DFAState> states = new HashMap<>();
   public volatile DFAState s0;
   public final int decision;
   public final DecisionState atnStartState;
   private final boolean precedenceDfa;

   public DFA(DecisionState atnStartState, int decision) {
      this.atnStartState = atnStartState;
      this.decision = decision;
      boolean precedenceDfa = false;
      if (atnStartState instanceof StarLoopEntryState && ((StarLoopEntryState)atnStartState).isPrecedenceDecision) {
         precedenceDfa = true;
         DFAState precedenceState = new DFAState(new ATNConfigSet());
         precedenceState.edges = new DFAState[0];
         precedenceState.isAcceptState = false;
         precedenceState.requiresFullContext = false;
         this.s0 = precedenceState;
      }

      this.precedenceDfa = precedenceDfa;
   }

   public final boolean isPrecedenceDfa() {
      return this.precedenceDfa;
   }

   public final DFAState getPrecedenceStartState(int precedence) {
      if (!this.isPrecedenceDfa()) {
         throw new IllegalStateException("Only precedence DFAs may contain a precedence start state.");
      } else {
         return precedence >= 0 && precedence < this.s0.edges.length ? this.s0.edges[precedence] : null;
      }
   }

   public final void setPrecedenceStartState(int precedence, DFAState startState) {
      if (!this.isPrecedenceDfa()) {
         throw new IllegalStateException("Only precedence DFAs may contain a precedence start state.");
      } else if (precedence >= 0) {
         synchronized (this.s0) {
            if (precedence >= this.s0.edges.length) {
               this.s0.edges = Arrays.copyOf(this.s0.edges, precedence + 1);
            }

            this.s0.edges[precedence] = startState;
         }
      }
   }

   public List<DFAState> getStates() {
      List<DFAState> result = new ArrayList<>(this.states.keySet());
      Collections.sort(result, new Comparator<DFAState>() {
         public int compare(DFAState o1, DFAState o2) {
            return o1.stateNumber - o2.stateNumber;
         }
      });
      return result;
   }

   @Override
   public String toString() {
      return this.toString(VocabularyImpl.EMPTY_VOCABULARY);
   }

   public String toString(Vocabulary vocabulary) {
      if (this.s0 == null) {
         return "";
      } else {
         DFASerializer serializer = new DFASerializer(this, vocabulary);
         return serializer.toString();
      }
   }
}
