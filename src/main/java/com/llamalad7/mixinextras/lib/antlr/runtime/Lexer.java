package com.llamalad7.mixinextras.lib.antlr.runtime;

import com.llamalad7.mixinextras.lib.antlr.runtime.atn.LexerATNSimulator;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.IntegerStack;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.Interval;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.Pair;
import java.util.EmptyStackException;

public abstract class Lexer extends Recognizer<Integer, LexerATNSimulator> implements TokenSource {
   public CharStream _input;
   protected Pair<TokenSource, CharStream> _tokenFactorySourcePair;
   protected TokenFactory<?> _factory = CommonTokenFactory.DEFAULT;
   public Token _token;
   public int _tokenStartCharIndex = -1;
   public int _tokenStartLine;
   public int _tokenStartCharPositionInLine;
   public boolean _hitEOF;
   public int _channel;
   public int _type;
   public final IntegerStack _modeStack = new IntegerStack();
   public int _mode = 0;
   public String _text;

   public Lexer(CharStream input) {
      this._input = input;
      this._tokenFactorySourcePair = new Pair<>(this, input);
   }

   @Override
   public Token nextToken() {
      if (this._input == null) {
         throw new IllegalStateException("nextToken requires a non-null input stream.");
      } else {
         int tokenStartMarker = this._input.mark();

         Token var10;
         try {
            label97:
            while (!this._hitEOF) {
               this._token = null;
               this._channel = 0;
               this._tokenStartCharIndex = this._input.index();
               this._tokenStartCharPositionInLine = this.getInterpreter().getCharPositionInLine();
               this._tokenStartLine = this.getInterpreter().getLine();
               this._text = null;

               do {
                  this._type = 0;

                  try {
                     ttype = this.getInterpreter().match(this._input, this._mode);
                  } catch (LexerNoViableAltException var7) {
                     this.notifyListeners(var7);
                     this.recover(var7);
                     ttype = -3;
                  }

                  if (this._input.LA(1) == -1) {
                     this._hitEOF = true;
                  }

                  if (this._type == 0) {
                     this._type = ttype;
                  }

                  if (this._type == -3) {
                     continue label97;
                  }
               } while (this._type == -2);

               if (this._token == null) {
                  this.emit();
               }

               return this._token;
            }

            this.emitEOF();
            var10 = this._token;
         } finally {
            this._input.release(tokenStartMarker);
         }

         return var10;
      }
   }

   public void skip() {
      this._type = -3;
   }

   public void more() {
      this._type = -2;
   }

   public void mode(int m) {
      this._mode = m;
   }

   public void pushMode(int m) {
      this._modeStack.push(this._mode);
      this.mode(m);
   }

   public int popMode() {
      if (this._modeStack.isEmpty()) {
         throw new EmptyStackException();
      } else {
         this.mode(this._modeStack.pop());
         return this._mode;
      }
   }

   @Override
   public TokenFactory<? extends Token> getTokenFactory() {
      return (TokenFactory<? extends Token>)this._factory;
   }

   @Override
   public CharStream getInputStream() {
      return this._input;
   }

   public void emit(Token token) {
      this._token = token;
   }

   public Token emit() {
      Token t = this._factory
         .create(
            this._tokenFactorySourcePair,
            this._type,
            this._text,
            this._channel,
            this._tokenStartCharIndex,
            this.getCharIndex() - 1,
            this._tokenStartLine,
            this._tokenStartCharPositionInLine
         );
      this.emit(t);
      return t;
   }

   public Token emitEOF() {
      int cpos = this.getCharPositionInLine();
      int line = this.getLine();
      Token eof = this._factory.create(this._tokenFactorySourcePair, -1, null, 0, this._input.index(), this._input.index() - 1, line, cpos);
      this.emit(eof);
      return eof;
   }

   @Override
   public int getLine() {
      return this.getInterpreter().getLine();
   }

   @Override
   public int getCharPositionInLine() {
      return this.getInterpreter().getCharPositionInLine();
   }

   public int getCharIndex() {
      return this._input.index();
   }

   public String getText() {
      return this._text != null ? this._text : this.getInterpreter().getText(this._input);
   }

   public void setText(String text) {
      this._text = text;
   }

   public void setType(int ttype) {
      this._type = ttype;
   }

   public void setChannel(int channel) {
      this._channel = channel;
   }

   @Deprecated
   @Override
   public String[] getTokenNames() {
      return null;
   }

   public void recover(LexerNoViableAltException e) {
      if (this._input.LA(1) != -1) {
         this.getInterpreter().consume(this._input);
      }
   }

   public void notifyListeners(LexerNoViableAltException e) {
      String text = this._input.getText(Interval.of(this._tokenStartCharIndex, this._input.index()));
      String msg = "token recognition error at: '" + this.getErrorDisplay(text) + "'";
      ANTLRErrorListener listener = this.getErrorListenerDispatch();
      listener.syntaxError(this, null, this._tokenStartLine, this._tokenStartCharPositionInLine, msg, e);
   }

   public String getErrorDisplay(String s) {
      StringBuilder buf = new StringBuilder();

      for (char c : s.toCharArray()) {
         buf.append(this.getErrorDisplay(c));
      }

      return buf.toString();
   }

   public String getErrorDisplay(int c) {
      String s = String.valueOf((char)c);
      switch (c) {
         case -1:
            s = "<EOF>";
            break;
         case 9:
            s = "\\t";
            break;
         case 10:
            s = "\\n";
            break;
         case 13:
            s = "\\r";
      }

      return s;
   }
}
