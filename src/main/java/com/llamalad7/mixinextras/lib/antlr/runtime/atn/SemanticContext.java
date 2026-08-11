package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.Recognizer;
import com.llamalad7.mixinextras.lib.antlr.runtime.RuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class SemanticContext {
   public abstract boolean eval(Recognizer<?, ?> var1, RuleContext var2);

   public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
      return this;
   }

   public static SemanticContext and(SemanticContext a, SemanticContext b) {
      if (a == null || a == SemanticContext.Empty.Instance) {
         return b;
      } else if (b != null && b != SemanticContext.Empty.Instance) {
         SemanticContext.AND result = new SemanticContext.AND(a, b);
         return (SemanticContext)(result.opnds.length == 1 ? result.opnds[0] : result);
      } else {
         return a;
      }
   }

   public static SemanticContext or(SemanticContext a, SemanticContext b) {
      if (a == null) {
         return b;
      } else if (b == null) {
         return a;
      } else if (a != SemanticContext.Empty.Instance && b != SemanticContext.Empty.Instance) {
         SemanticContext.OR result = new SemanticContext.OR(a, b);
         return (SemanticContext)(result.opnds.length == 1 ? result.opnds[0] : result);
      } else {
         return SemanticContext.Empty.Instance;
      }
   }

   private static List<SemanticContext.PrecedencePredicate> filterPrecedencePredicates(Collection<? extends SemanticContext> collection) {
      ArrayList<SemanticContext.PrecedencePredicate> result = null;
      Iterator<? extends SemanticContext> iterator = collection.iterator();

      while (iterator.hasNext()) {
         SemanticContext context = iterator.next();
         if (context instanceof SemanticContext.PrecedencePredicate) {
            if (result == null) {
               result = new ArrayList<>();
            }

            result.add((SemanticContext.PrecedencePredicate)context);
            iterator.remove();
         }
      }

      return (List<SemanticContext.PrecedencePredicate>)(result == null ? Collections.emptyList() : result);
   }

   public static class AND extends SemanticContext.Operator {
      public final SemanticContext[] opnds;

      public AND(SemanticContext a, SemanticContext b) {
         Set<SemanticContext> operands = new HashSet<>();
         if (a instanceof SemanticContext.AND) {
            operands.addAll(Arrays.asList(((SemanticContext.AND)a).opnds));
         } else {
            operands.add(a);
         }

         if (b instanceof SemanticContext.AND) {
            operands.addAll(Arrays.asList(((SemanticContext.AND)b).opnds));
         } else {
            operands.add(b);
         }

         List<SemanticContext.PrecedencePredicate> precedencePredicates = SemanticContext.filterPrecedencePredicates(operands);
         if (!precedencePredicates.isEmpty()) {
            SemanticContext.PrecedencePredicate reduced = Collections.min(precedencePredicates);
            operands.add(reduced);
         }

         this.opnds = operands.toArray(new SemanticContext[0]);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof SemanticContext.AND)) {
            return false;
         } else {
            SemanticContext.AND other = (SemanticContext.AND)obj;
            return Arrays.equals((Object[])this.opnds, (Object[])other.opnds);
         }
      }

      @Override
      public int hashCode() {
         return MurmurHash.hashCode(this.opnds, SemanticContext.AND.class.hashCode());
      }

      @Override
      public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         for (SemanticContext opnd : this.opnds) {
            if (!opnd.eval(parser, parserCallStack)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         boolean differs = false;
         List<SemanticContext> operands = new ArrayList<>();

         for (SemanticContext context : this.opnds) {
            SemanticContext evaluated = context.evalPrecedence(parser, parserCallStack);
            differs |= evaluated != context;
            if (evaluated == null) {
               return null;
            }

            if (evaluated != SemanticContext.Empty.Instance) {
               operands.add(evaluated);
            }
         }

         if (!differs) {
            return this;
         } else if (operands.isEmpty()) {
            return SemanticContext.Empty.Instance;
         } else {
            SemanticContext result = operands.get(0);

            for (int i = 1; i < operands.size(); i++) {
               result = SemanticContext.and(result, operands.get(i));
            }

            return result;
         }
      }

      @Override
      public String toString() {
         return Utils.join(Arrays.asList(this.opnds).iterator(), "&&");
      }
   }

   public static class Empty extends SemanticContext {
      public static final SemanticContext.Empty Instance = new SemanticContext.Empty();

      @Override
      public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         return false;
      }
   }

   public static class OR extends SemanticContext.Operator {
      public final SemanticContext[] opnds;

      public OR(SemanticContext a, SemanticContext b) {
         Set<SemanticContext> operands = new HashSet<>();
         if (a instanceof SemanticContext.OR) {
            operands.addAll(Arrays.asList(((SemanticContext.OR)a).opnds));
         } else {
            operands.add(a);
         }

         if (b instanceof SemanticContext.OR) {
            operands.addAll(Arrays.asList(((SemanticContext.OR)b).opnds));
         } else {
            operands.add(b);
         }

         List<SemanticContext.PrecedencePredicate> precedencePredicates = SemanticContext.filterPrecedencePredicates(operands);
         if (!precedencePredicates.isEmpty()) {
            SemanticContext.PrecedencePredicate reduced = Collections.max(precedencePredicates);
            operands.add(reduced);
         }

         this.opnds = operands.toArray(new SemanticContext[0]);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof SemanticContext.OR)) {
            return false;
         } else {
            SemanticContext.OR other = (SemanticContext.OR)obj;
            return Arrays.equals((Object[])this.opnds, (Object[])other.opnds);
         }
      }

      @Override
      public int hashCode() {
         return MurmurHash.hashCode(this.opnds, SemanticContext.OR.class.hashCode());
      }

      @Override
      public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         for (SemanticContext opnd : this.opnds) {
            if (opnd.eval(parser, parserCallStack)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         boolean differs = false;
         List<SemanticContext> operands = new ArrayList<>();

         for (SemanticContext context : this.opnds) {
            SemanticContext evaluated = context.evalPrecedence(parser, parserCallStack);
            differs |= evaluated != context;
            if (evaluated == SemanticContext.Empty.Instance) {
               return SemanticContext.Empty.Instance;
            }

            if (evaluated != null) {
               operands.add(evaluated);
            }
         }

         if (!differs) {
            return this;
         } else if (operands.isEmpty()) {
            return null;
         } else {
            SemanticContext result = operands.get(0);

            for (int i = 1; i < operands.size(); i++) {
               result = SemanticContext.or(result, operands.get(i));
            }

            return result;
         }
      }

      @Override
      public String toString() {
         return Utils.join(Arrays.asList(this.opnds).iterator(), "||");
      }
   }

   public abstract static class Operator extends SemanticContext {
   }

   public static class PrecedencePredicate extends SemanticContext implements Comparable<SemanticContext.PrecedencePredicate> {
      public final int precedence;

      protected PrecedencePredicate() {
         this.precedence = 0;
      }

      public PrecedencePredicate(int precedence) {
         this.precedence = precedence;
      }

      @Override
      public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         return parser.precpred(parserCallStack, this.precedence);
      }

      @Override
      public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         return parser.precpred(parserCallStack, this.precedence) ? SemanticContext.Empty.Instance : null;
      }

      public int compareTo(SemanticContext.PrecedencePredicate o) {
         return this.precedence - o.precedence;
      }

      @Override
      public int hashCode() {
         int hashCode = 1;
         return 31 * hashCode + this.precedence;
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof SemanticContext.PrecedencePredicate)) {
            return false;
         } else if (this == obj) {
            return true;
         } else {
            SemanticContext.PrecedencePredicate other = (SemanticContext.PrecedencePredicate)obj;
            return this.precedence == other.precedence;
         }
      }

      @Override
      public String toString() {
         return "{" + this.precedence + ">=prec}?";
      }
   }

   public static class Predicate extends SemanticContext {
      public final int ruleIndex;
      public final int predIndex;
      public final boolean isCtxDependent;

      protected Predicate() {
         this.ruleIndex = -1;
         this.predIndex = -1;
         this.isCtxDependent = false;
      }

      public Predicate(int ruleIndex, int predIndex, boolean isCtxDependent) {
         this.ruleIndex = ruleIndex;
         this.predIndex = predIndex;
         this.isCtxDependent = isCtxDependent;
      }

      @Override
      public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
         RuleContext localctx = this.isCtxDependent ? parserCallStack : null;
         return parser.sempred(localctx, this.ruleIndex, this.predIndex);
      }

      @Override
      public int hashCode() {
         int hashCode = MurmurHash.initialize();
         hashCode = MurmurHash.update(hashCode, this.ruleIndex);
         hashCode = MurmurHash.update(hashCode, this.predIndex);
         hashCode = MurmurHash.update(hashCode, this.isCtxDependent ? 1 : 0);
         return MurmurHash.finish(hashCode, 3);
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof SemanticContext.Predicate)) {
            return false;
         } else if (this == obj) {
            return true;
         } else {
            SemanticContext.Predicate p = (SemanticContext.Predicate)obj;
            return this.ruleIndex == p.ruleIndex && this.predIndex == p.predIndex && this.isCtxDependent == p.isCtxDependent;
         }
      }

      @Override
      public String toString() {
         return "{" + this.ruleIndex + ":" + this.predIndex + "}?";
      }
   }
}
