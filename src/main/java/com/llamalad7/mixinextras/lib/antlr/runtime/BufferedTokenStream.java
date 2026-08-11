package com.llamalad7.mixinextras.lib.antlr.runtime;

import com.llamalad7.mixinextras.lib.antlr.runtime.misc.Interval;
import java.util.ArrayList;
import java.util.List;

public class BufferedTokenStream implements TokenStream {
   protected TokenSource tokenSource;
   protected List<Token> tokens = new ArrayList<>(100);
   protected int p = -1;
   protected boolean fetchedEOF;

   public BufferedTokenStream(TokenSource tokenSource) {
      if (tokenSource == null) {
         throw new NullPointerException("tokenSource cannot be null");
      } else {
         this.tokenSource = tokenSource;
      }
   }

   @Override
   public TokenSource getTokenSource() {
      return this.tokenSource;
   }

   @Override
   public int index() {
      return this.p;
   }

   @Override
   public int mark() {
      return 0;
   }

   @Override
   public void release(int marker) {
   }

   @Override
   public void seek(int index) {
      this.lazyInit();
      this.p = this.adjustSeekIndex(index);
   }

   @Override
   public int size() {
      return this.tokens.size();
   }

   @Override
   public void consume() {
      boolean skipEofCheck;
      if (this.p >= 0) {
         if (this.fetchedEOF) {
            skipEofCheck = this.p < this.tokens.size() - 1;
         } else {
            skipEofCheck = this.p < this.tokens.size();
         }
      } else {
         skipEofCheck = false;
      }

      if (!skipEofCheck && this.LA(1) == -1) {
         throw new IllegalStateException("cannot consume EOF");
      } else {
         if (this.sync(this.p + 1)) {
            this.p = this.adjustSeekIndex(this.p + 1);
         }
      }
   }

   protected boolean sync(int i) {
      assert i >= 0;

      int n = i - this.tokens.size() + 1;
      if (n > 0) {
         int fetched = this.fetch(n);
         return fetched >= n;
      } else {
         return true;
      }
   }

   protected int fetch(int n) {
      if (this.fetchedEOF) {
         return 0;
      } else {
         for (int i = 0; i < n; i++) {
            Token t = this.tokenSource.nextToken();
            if (t instanceof WritableToken) {
               ((WritableToken)t).setTokenIndex(this.tokens.size());
            }

            this.tokens.add(t);
            if (t.getType() == -1) {
               this.fetchedEOF = true;
               return i + 1;
            }
         }

         return n;
      }
   }

   @Override
   public Token get(int i) {
      if (i >= 0 && i < this.tokens.size()) {
         return this.tokens.get(i);
      } else {
         throw new IndexOutOfBoundsException("token index " + i + " out of range 0.." + (this.tokens.size() - 1));
      }
   }

   @Override
   public int LA(int i) {
      return this.LT(i).getType();
   }

   protected Token LB(int k) {
      return this.p - k < 0 ? null : this.tokens.get(this.p - k);
   }

   @Override
   public Token LT(int k) {
      this.lazyInit();
      if (k == 0) {
         return null;
      } else if (k < 0) {
         return this.LB(-k);
      } else {
         int i = this.p + k - 1;
         this.sync(i);
         return i >= this.tokens.size() ? this.tokens.get(this.tokens.size() - 1) : this.tokens.get(i);
      }
   }

   protected int adjustSeekIndex(int i) {
      return i;
   }

   protected final void lazyInit() {
      if (this.p == -1) {
         this.setup();
      }
   }

   protected void setup() {
      this.sync(0);
      this.p = this.adjustSeekIndex(0);
   }

   protected int nextTokenOnChannel(int i, int channel) {
      this.sync(i);
      if (i >= this.size()) {
         return this.size() - 1;
      } else {
         for (Token token = this.tokens.get(i); token.getChannel() != channel; token = this.tokens.get(i)) {
            if (token.getType() == -1) {
               return i;
            }

            this.sync(++i);
         }

         return i;
      }
   }

   protected int previousTokenOnChannel(int i, int channel) {
      this.sync(i);
      if (i >= this.size()) {
         return this.size() - 1;
      } else {
         while (i >= 0) {
            Token token = this.tokens.get(i);
            if (token.getType() == -1 || token.getChannel() == channel) {
               return i;
            }

            i--;
         }

         return i;
      }
   }

   @Override
   public String getText(Interval interval) {
      int start = interval.a;
      int stop = interval.b;
      if (start >= 0 && stop >= 0) {
         this.sync(stop);
         if (stop >= this.tokens.size()) {
            stop = this.tokens.size() - 1;
         }

         StringBuilder buf = new StringBuilder();

         for (int i = start; i <= stop; i++) {
            Token t = this.tokens.get(i);
            if (t.getType() == -1) {
               break;
            }

            buf.append(t.getText());
         }

         return buf.toString();
      } else {
         return "";
      }
   }

   @Override
   public String getText(Token start, Token stop) {
      return start != null && stop != null ? this.getText(Interval.of(start.getTokenIndex(), stop.getTokenIndex())) : "";
   }
}
