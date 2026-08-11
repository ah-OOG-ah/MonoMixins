package com.llamalad7.mixinextras.lib.grammar.expressions;

import com.llamalad7.mixinextras.lib.antlr.runtime.FailedPredicateException;
import com.llamalad7.mixinextras.lib.antlr.runtime.NoViableAltException;
import com.llamalad7.mixinextras.lib.antlr.runtime.Parser;
import com.llamalad7.mixinextras.lib.antlr.runtime.ParserRuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.RecognitionException;
import com.llamalad7.mixinextras.lib.antlr.runtime.RuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.RuntimeMetaData;
import com.llamalad7.mixinextras.lib.antlr.runtime.Token;
import com.llamalad7.mixinextras.lib.antlr.runtime.TokenStream;
import com.llamalad7.mixinextras.lib.antlr.runtime.Vocabulary;
import com.llamalad7.mixinextras.lib.antlr.runtime.VocabularyImpl;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.ATN;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.ATNDeserializer;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.ParserATNSimulator;
import com.llamalad7.mixinextras.lib.antlr.runtime.atn.PredictionContextCache;
import com.llamalad7.mixinextras.lib.antlr.runtime.dfa.DFA;
import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ParseTreeListener;
import java.util.ArrayList;
import java.util.List;

public class ExpressionParser extends Parser {
   protected static final DFA[] _decisionToDFA;
   protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
   public static final String[] ruleNames = makeRuleNames();
   private static final String[] _LITERAL_NAMES = makeLiteralNames();
   private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
   public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
   @Deprecated
   public static final String[] tokenNames = new String[_SYMBOLIC_NAMES.length];
   public static final ATN _ATN;

   private static String[] makeRuleNames() {
      return new String[]{"root", "statement", "expression", "name", "nameWithDims", "arguments", "nonEmptyArguments"};
   }

   private static String[] makeLiteralNames() {
      return new String[]{
         null,
         null,
         null,
         null,
         "'?'",
         "'new'",
         "'instanceof'",
         null,
         "'null'",
         "'return'",
         "'throw'",
         "'this'",
         "'super'",
         "'class'",
         null,
         null,
         null,
         null,
         "'+'",
         "'-'",
         "'*'",
         "'/'",
         "'%'",
         "'~'",
         "'.'",
         "','",
         "'('",
         "')'",
         "'['",
         "']'",
         "'{'",
         "'}'",
         "'@'",
         "'<<'",
         "'>>'",
         "'>>>'",
         "'<'",
         "'<='",
         "'>'",
         "'>='",
         "'=='",
         "'!='",
         "'&'",
         "'^'",
         "'|'",
         "'='",
         "'::'",
         "'++'",
         "'--'"
      };
   }

   private static String[] makeSymbolicNames() {
      return new String[]{
         null,
         "NewLine",
         "WS",
         "StringLit",
         "Wildcard",
         "New",
         "Instanceof",
         "BoolLit",
         "NullLit",
         "Return",
         "Throw",
         "This",
         "Super",
         "Class",
         "Reserved",
         "Identifier",
         "IntLit",
         "DecLit",
         "Plus",
         "Minus",
         "Mult",
         "Div",
         "Mod",
         "BitwiseNot",
         "Dot",
         "Comma",
         "LeftParen",
         "RightParen",
         "LeftBracket",
         "RightBracket",
         "LeftBrace",
         "RightBrace",
         "At",
         "Shl",
         "Shr",
         "Ushr",
         "Lt",
         "Le",
         "Gt",
         "Ge",
         "Eq",
         "Ne",
         "BitwiseAnd",
         "BitwiseXor",
         "BitwiseOr",
         "Assign",
         "MethodRef",
         "Increment",
         "Decrement"
      };
   }

   @Deprecated
   @Override
   public String[] getTokenNames() {
      return tokenNames;
   }

   @Override
   public Vocabulary getVocabulary() {
      return VOCABULARY;
   }

   @Override
   public String[] getRuleNames() {
      return ruleNames;
   }

   @Override
   public ATN getATN() {
      return _ATN;
   }

   public ExpressionParser(TokenStream input) {
      super(input);
      this._interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
   }

   public final ExpressionParser.RootContext root() throws RecognitionException {
      ExpressionParser.RootContext _localctx = new ExpressionParser.RootContext(this._ctx, this.getState());
      this.enterRule(_localctx, 0, 0);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(14);
         this.statement();
         this.setState(15);
         this.match(-1);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.StatementContext statement() throws RecognitionException {
      ExpressionParser.StatementContext _localctx = new ExpressionParser.StatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 2, 1);

      try {
         this.setState(39);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 0, this._ctx)) {
            case 1:
               _localctx = new ExpressionParser.MemberAssignmentStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(17);
               ((ExpressionParser.MemberAssignmentStatementContext)_localctx).receiver = this.expression(0);
               this.setState(18);
               this.match(24);
               this.setState(19);
               ((ExpressionParser.MemberAssignmentStatementContext)_localctx).memberName = this.name();
               this.setState(20);
               this.match(45);
               this.setState(21);
               ((ExpressionParser.MemberAssignmentStatementContext)_localctx).value = this.expression(0);
               break;
            case 2:
               _localctx = new ExpressionParser.ArrayStoreStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(23);
               ((ExpressionParser.ArrayStoreStatementContext)_localctx).arr = this.expression(0);
               this.setState(24);
               this.match(28);
               this.setState(25);
               ((ExpressionParser.ArrayStoreStatementContext)_localctx).index = this.expression(0);
               this.setState(26);
               this.match(29);
               this.setState(27);
               this.match(45);
               this.setState(28);
               ((ExpressionParser.ArrayStoreStatementContext)_localctx).value = this.expression(0);
               break;
            case 3:
               _localctx = new ExpressionParser.IdentifierAssignmentStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 3);
               this.setState(30);
               ((ExpressionParser.IdentifierAssignmentStatementContext)_localctx).identifier = this.name();
               this.setState(31);
               this.match(45);
               this.setState(32);
               ((ExpressionParser.IdentifierAssignmentStatementContext)_localctx).value = this.expression(0);
               break;
            case 4:
               _localctx = new ExpressionParser.ReturnStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 4);
               this.setState(34);
               this.match(9);
               this.setState(35);
               ((ExpressionParser.ReturnStatementContext)_localctx).value = this.expression(0);
               break;
            case 5:
               _localctx = new ExpressionParser.ThrowStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 5);
               this.setState(36);
               this.match(10);
               this.setState(37);
               ((ExpressionParser.ThrowStatementContext)_localctx).value = this.expression(0);
               break;
            case 6:
               _localctx = new ExpressionParser.ExpressionStatementContext(_localctx);
               this.enterOuterAlt(_localctx, 6);
               this.setState(38);
               this.expression(0);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   private ExpressionParser.ExpressionContext expression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.ExpressionContext _localctx = new ExpressionParser.ExpressionContext(this._ctx, _parentState);
      int _startState = 4;
      this.enterRecursionRule(_localctx, 4, 2, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(125);
         this._errHandler.sync(this);
         label371:
         switch (this.getInterpreter().adaptivePredict(this._input, 5, this._ctx)) {
            case 1:
               _localctx = new ExpressionParser.CapturingExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(42);
               this.match(32);
               this.setState(43);
               this.match(26);
               this.setState(44);
               ((ExpressionParser.CapturingExpressionContext)_localctx).expr = this.expression(0);
               this.setState(45);
               this.match(27);
               break;
            case 2:
               _localctx = new ExpressionParser.WildcardExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(47);
               this.match(4);
               break;
            case 3:
               _localctx = new ExpressionParser.ThisExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(48);
               this.match(11);
               break;
            case 4:
               _localctx = new ExpressionParser.IntLitExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(50);
               this._errHandler.sync(this);
               int _la = this._input.LA(1);
               if (_la == 19) {
                  this.setState(49);
                  ((ExpressionParser.IntLitExpressionContext)_localctx).lit = this.match(19);
               }

               this.setState(52);
               this.match(16);
               break;
            case 5:
               _localctx = new ExpressionParser.DecimalLitExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(54);
               this._errHandler.sync(this);
               int _la = this._input.LA(1);
               if (_la == 19) {
                  this.setState(53);
                  ((ExpressionParser.DecimalLitExpressionContext)_localctx).lit = this.match(19);
               }

               this.setState(56);
               this.match(17);
               break;
            case 6:
               _localctx = new ExpressionParser.BoolLitExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(57);
               ((ExpressionParser.BoolLitExpressionContext)_localctx).lit = this.match(7);
               break;
            case 7:
               _localctx = new ExpressionParser.NullExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(58);
               ((ExpressionParser.NullExpressionContext)_localctx).lit = this.match(8);
               break;
            case 8:
               _localctx = new ExpressionParser.StringLitExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(59);
               ((ExpressionParser.StringLitExpressionContext)_localctx).lit = this.match(3);
               break;
            case 9:
               _localctx = new ExpressionParser.IdentifierExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(60);
               ((ExpressionParser.IdentifierExpressionContext)_localctx).id = this.match(15);
               break;
            case 10:
               _localctx = new ExpressionParser.ClassConstantExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(61);
               ((ExpressionParser.ClassConstantExpressionContext)_localctx).type = this.nameWithDims();
               this.setState(62);
               this.match(24);
               this.setState(63);
               this.match(13);
               break;
            case 11:
               _localctx = new ExpressionParser.SuperCallExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(65);
               this.match(12);
               this.setState(66);
               this.match(24);
               this.setState(67);
               ((ExpressionParser.SuperCallExpressionContext)_localctx).memberName = this.name();
               this.setState(68);
               this.match(26);
               this.setState(69);
               ((ExpressionParser.SuperCallExpressionContext)_localctx).args = this.arguments();
               this.setState(70);
               this.match(27);
               break;
            case 12:
               _localctx = new ExpressionParser.StaticMethodCallExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(72);
               ((ExpressionParser.StaticMethodCallExpressionContext)_localctx).memberName = this.name();
               this.setState(73);
               this.match(26);
               this.setState(74);
               ((ExpressionParser.StaticMethodCallExpressionContext)_localctx).args = this.arguments();
               this.setState(75);
               this.match(27);
               break;
            case 13:
               _localctx = new ExpressionParser.FreeMethodReferenceExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(77);
               this.match(46);
               this.setState(78);
               ((ExpressionParser.FreeMethodReferenceExpressionContext)_localctx).memberName = this.name();
               break;
            case 14:
               _localctx = new ExpressionParser.ConstructorReferenceExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(79);
               ((ExpressionParser.ConstructorReferenceExpressionContext)_localctx).type = this.name();
               this.setState(80);
               this.match(46);
               this.setState(81);
               this.match(5);
               break;
            case 15:
               _localctx = new ExpressionParser.UnaryExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(83);
               ((ExpressionParser.UnaryExpressionContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 19 && _la != 23) {
                  ((ExpressionParser.UnaryExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }

               this.setState(84);
               ((ExpressionParser.UnaryExpressionContext)_localctx).expr = this.expression(15);
               break;
            case 16:
               _localctx = new ExpressionParser.InstantiationExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(85);
               this.match(5);
               this.setState(86);
               ((ExpressionParser.InstantiationExpressionContext)_localctx).type = this.name();
               this.setState(87);
               this.match(26);
               this.setState(88);
               ((ExpressionParser.InstantiationExpressionContext)_localctx).args = this.arguments();
               this.setState(89);
               this.match(27);
               break;
            case 17:
               _localctx = new ExpressionParser.ArrayLitExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(91);
               this.match(5);
               this.setState(92);
               ((ExpressionParser.ArrayLitExpressionContext)_localctx).elementType = this.nameWithDims();
               this.setState(93);
               this.match(28);
               this.setState(94);
               this.match(29);
               this.setState(95);
               this.match(30);
               this.setState(96);
               ((ExpressionParser.ArrayLitExpressionContext)_localctx).values = this.nonEmptyArguments();
               this.setState(97);
               this.match(31);
               break;
            case 18:
               _localctx = new ExpressionParser.NewArrayExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(99);
               this.match(5);
               this.setState(100);
               ((ExpressionParser.NewArrayExpressionContext)_localctx).innerType = this.name();
               this.setState(105);
               this._errHandler.sync(this);
               int _alt = 1;

               while (true) {
                  switch (_alt) {
                     case 1:
                        this.setState(101);
                        this.match(28);
                        this.setState(102);
                        ((ExpressionParser.NewArrayExpressionContext)_localctx).expression = this.expression(0);
                        ((ExpressionParser.NewArrayExpressionContext)_localctx).dims.add(((ExpressionParser.NewArrayExpressionContext)_localctx).expression);
                        this.setState(103);
                        this.match(29);
                        this.setState(107);
                        this._errHandler.sync(this);
                        _alt = this.getInterpreter().adaptivePredict(this._input, 3, this._ctx);
                        if (_alt == 2 || _alt == 0) {
                           this.setState(113);
                           this._errHandler.sync(this);
                           _alt = this.getInterpreter().adaptivePredict(this._input, 4, this._ctx);

                           while (true) {
                              if (_alt == 2 || _alt == 0) {
                                 break label371;
                              }

                              if (_alt == 1) {
                                 this.setState(109);
                                 ((ExpressionParser.NewArrayExpressionContext)_localctx).LeftBracket = this.match(28);
                                 ((ExpressionParser.NewArrayExpressionContext)_localctx)
                                    .blankDims
                                    .add(((ExpressionParser.NewArrayExpressionContext)_localctx).LeftBracket);
                                 this.setState(110);
                                 this.match(29);
                              }

                              this.setState(115);
                              this._errHandler.sync(this);
                              _alt = this.getInterpreter().adaptivePredict(this._input, 4, this._ctx);
                           }
                        }
                        break;
                     default:
                        throw new NoViableAltException(this);
                  }
               }
            case 19:
               _localctx = new ExpressionParser.CastExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(116);
               this.match(26);
               this.setState(117);
               ((ExpressionParser.CastExpressionContext)_localctx).type = this.nameWithDims();
               this.setState(118);
               this.match(27);
               this.setState(119);
               ((ExpressionParser.CastExpressionContext)_localctx).expr = this.expression(11);
               break;
            case 20:
               _localctx = new ExpressionParser.ParenthesizedExpressionContext(_localctx);
               this._ctx = _localctx;
               this.setState(121);
               this.match(26);
               this.setState(122);
               ((ExpressionParser.ParenthesizedExpressionContext)_localctx).expr = this.expression(0);
               this.setState(123);
               this.match(27);
         }

         this._ctx.stop = this._input.LT(-1);
         this.setState(174);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 7, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 7, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.ExpressionContext _prevctx = _localctx;
               this.setState(172);
               this._errHandler.sync(this);
               switch (this.getInterpreter().adaptivePredict(this._input, 6, this._ctx)) {
                  case 1:
                     _localctx = new ExpressionParser.MultiplicativeExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.MultiplicativeExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(127);
                     if (!this.precpred(this._ctx, 10)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 10)");
                     }

                     this.setState(128);
                     ((ExpressionParser.MultiplicativeExpressionContext)_localctx).op = this._input.LT(1);
                     int _la = this._input.LA(1);
                     if ((_la & -64) == 0 && (1L << _la & 7340032L) != 0L) {
                        if (this._input.LA(1) == -1) {
                           this.matchedEOF = true;
                        }

                        this._errHandler.reportMatch(this);
                        this.consume();
                     } else {
                        ((ExpressionParser.MultiplicativeExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
                     }

                     this.setState(129);
                     ((ExpressionParser.MultiplicativeExpressionContext)_localctx).right = this.expression(11);
                     break;
                  case 2:
                     _localctx = new ExpressionParser.AdditiveExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.AdditiveExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(130);
                     if (!this.precpred(this._ctx, 9)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 9)");
                     }

                     this.setState(131);
                     ((ExpressionParser.AdditiveExpressionContext)_localctx).op = this._input.LT(1);
                     int _la = this._input.LA(1);
                     if (_la != 18 && _la != 19) {
                        ((ExpressionParser.AdditiveExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
                     } else {
                        if (this._input.LA(1) == -1) {
                           this.matchedEOF = true;
                        }

                        this._errHandler.reportMatch(this);
                        this.consume();
                     }

                     this.setState(132);
                     ((ExpressionParser.AdditiveExpressionContext)_localctx).right = this.expression(10);
                     break;
                  case 3:
                     _localctx = new ExpressionParser.ShiftExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.ShiftExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(133);
                     if (!this.precpred(this._ctx, 8)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 8)");
                     }

                     this.setState(134);
                     ((ExpressionParser.ShiftExpressionContext)_localctx).op = this._input.LT(1);
                     int _la = this._input.LA(1);
                     if ((_la & -64) == 0 && (1L << _la & 60129542144L) != 0L) {
                        if (this._input.LA(1) == -1) {
                           this.matchedEOF = true;
                        }

                        this._errHandler.reportMatch(this);
                        this.consume();
                     } else {
                        ((ExpressionParser.ShiftExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
                     }

                     this.setState(135);
                     ((ExpressionParser.ShiftExpressionContext)_localctx).right = this.expression(9);
                     break;
                  case 4:
                     _localctx = new ExpressionParser.ComparisonExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.ComparisonExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(136);
                     if (!this.precpred(this._ctx, 7)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 7)");
                     }

                     this.setState(137);
                     ((ExpressionParser.ComparisonExpressionContext)_localctx).op = this._input.LT(1);
                     int _la = this._input.LA(1);
                     if ((_la & -64) == 0 && (1L << _la & 1030792151040L) != 0L) {
                        if (this._input.LA(1) == -1) {
                           this.matchedEOF = true;
                        }

                        this._errHandler.reportMatch(this);
                        this.consume();
                     } else {
                        ((ExpressionParser.ComparisonExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
                     }

                     this.setState(138);
                     ((ExpressionParser.ComparisonExpressionContext)_localctx).right = this.expression(8);
                     break;
                  case 5:
                     _localctx = new ExpressionParser.EqualityExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.EqualityExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(139);
                     if (!this.precpred(this._ctx, 5)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                     }

                     this.setState(140);
                     ((ExpressionParser.EqualityExpressionContext)_localctx).op = this._input.LT(1);
                     int _la = this._input.LA(1);
                     if (_la != 40 && _la != 41) {
                        ((ExpressionParser.EqualityExpressionContext)_localctx).op = this._errHandler.recoverInline(this);
                     } else {
                        if (this._input.LA(1) == -1) {
                           this.matchedEOF = true;
                        }

                        this._errHandler.reportMatch(this);
                        this.consume();
                     }

                     this.setState(141);
                     ((ExpressionParser.EqualityExpressionContext)_localctx).right = this.expression(6);
                     break;
                  case 6:
                     _localctx = new ExpressionParser.BitwiseAndExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.BitwiseAndExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(142);
                     if (!this.precpred(this._ctx, 4)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                     }

                     this.setState(143);
                     this.match(42);
                     this.setState(144);
                     ((ExpressionParser.BitwiseAndExpressionContext)_localctx).right = this.expression(5);
                     break;
                  case 7:
                     _localctx = new ExpressionParser.BitwiseXorExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.BitwiseXorExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(145);
                     if (!this.precpred(this._ctx, 3)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                     }

                     this.setState(146);
                     this.match(43);
                     this.setState(147);
                     ((ExpressionParser.BitwiseXorExpressionContext)_localctx).right = this.expression(4);
                     break;
                  case 8:
                     _localctx = new ExpressionParser.BitwiseOrExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.BitwiseOrExpressionContext)_localctx).left = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(148);
                     if (!this.precpred(this._ctx, 2)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                     }

                     this.setState(149);
                     this.match(44);
                     this.setState(150);
                     ((ExpressionParser.BitwiseOrExpressionContext)_localctx).right = this.expression(3);
                     break;
                  case 9:
                     _localctx = new ExpressionParser.ArrayAccessExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.ArrayAccessExpressionContext)_localctx).arr = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(151);
                     if (!this.precpred(this._ctx, 23)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 23)");
                     }

                     this.setState(152);
                     this.match(28);
                     this.setState(153);
                     ((ExpressionParser.ArrayAccessExpressionContext)_localctx).index = this.expression(0);
                     this.setState(154);
                     this.match(29);
                     break;
                  case 10:
                     _localctx = new ExpressionParser.MemberAccessExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.MemberAccessExpressionContext)_localctx).receiver = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(156);
                     if (!this.precpred(this._ctx, 22)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 22)");
                     }

                     this.setState(157);
                     this.match(24);
                     this.setState(158);
                     ((ExpressionParser.MemberAccessExpressionContext)_localctx).memberName = this.name();
                     break;
                  case 11:
                     _localctx = new ExpressionParser.MethodCallExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.MethodCallExpressionContext)_localctx).receiver = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(159);
                     if (!this.precpred(this._ctx, 20)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 20)");
                     }

                     this.setState(160);
                     this.match(24);
                     this.setState(161);
                     ((ExpressionParser.MethodCallExpressionContext)_localctx).memberName = this.name();
                     this.setState(162);
                     this.match(26);
                     this.setState(163);
                     ((ExpressionParser.MethodCallExpressionContext)_localctx).args = this.arguments();
                     this.setState(164);
                     this.match(27);
                     break;
                  case 12:
                     _localctx = new ExpressionParser.BoundMethodReferenceExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.BoundMethodReferenceExpressionContext)_localctx).receiver = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(166);
                     if (!this.precpred(this._ctx, 18)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 18)");
                     }

                     this.setState(167);
                     this.match(46);
                     this.setState(168);
                     ((ExpressionParser.BoundMethodReferenceExpressionContext)_localctx).memberName = this.name();
                     break;
                  case 13:
                     _localctx = new ExpressionParser.InstanceofExpressionContext(new ExpressionParser.ExpressionContext(_parentctx, _parentState));
                     ((ExpressionParser.InstanceofExpressionContext)_localctx).expr = (ExpressionParser.ExpressionContext)_prevctx;
                     this.pushNewRecursionContext(_localctx, _startState, 2);
                     this.setState(169);
                     if (!this.precpred(this._ctx, 6)) {
                        throw new FailedPredicateException(this, "precpred(_ctx, 6)");
                     }

                     this.setState(170);
                     this.match(6);
                     this.setState(171);
                     ((ExpressionParser.InstanceofExpressionContext)_localctx).type = this.nameWithDims();
               }
            }

            this.setState(176);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.NameContext name() throws RecognitionException {
      ExpressionParser.NameContext _localctx = new ExpressionParser.NameContext(this._ctx, this.getState());
      this.enterRule(_localctx, 6, 3);

      try {
         this.setState(179);
         this._errHandler.sync(this);
         switch (this._input.LA(1)) {
            case 4:
               _localctx = new ExpressionParser.WildcardNameContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(178);
               this.match(4);
               break;
            case 15:
               _localctx = new ExpressionParser.IdentifierNameContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(177);
               this.match(15);
               break;
            default:
               throw new NoViableAltException(this);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.NameWithDimsContext nameWithDims() throws RecognitionException {
      ExpressionParser.NameWithDimsContext _localctx = new ExpressionParser.NameWithDimsContext(this._ctx, this.getState());
      this.enterRule(_localctx, 8, 4);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(181);
         this.name();
         this.setState(186);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 9, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 9, this._ctx)
         ) {
            if (_alt == 1) {
               this.setState(182);
               _localctx.LeftBracket = this.match(28);
               _localctx.dims.add(_localctx.LeftBracket);
               this.setState(183);
               this.match(29);
            }

            this.setState(188);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ArgumentsContext arguments() throws RecognitionException {
      ExpressionParser.ArgumentsContext _localctx = new ExpressionParser.ArgumentsContext(this._ctx, this.getState());
      this.enterRule(_localctx, 10, 5);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(190);
         this._errHandler.sync(this);
         int _la = this._input.LA(1);
         if ((_la & -64) == 0 && (1L << _la & 70373115402680L) != 0L) {
            this.setState(189);
            this.nonEmptyArguments();
         }
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.NonEmptyArgumentsContext nonEmptyArguments() throws RecognitionException {
      ExpressionParser.NonEmptyArgumentsContext _localctx = new ExpressionParser.NonEmptyArgumentsContext(this._ctx, this.getState());
      this.enterRule(_localctx, 12, 6);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(197);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 11, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 11, this._ctx)
         ) {
            if (_alt == 1) {
               this.setState(192);
               this.expression(0);
               this.setState(193);
               this.match(25);
            }

            this.setState(199);
            this._errHandler.sync(this);
         }

         this.setState(200);
         this.expression(0);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   @Override
   public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
      switch (ruleIndex) {
         case 2:
            return this.expression_sempred((ExpressionParser.ExpressionContext)_localctx, predIndex);
         default:
            return true;
      }
   }

   private boolean expression_sempred(ExpressionParser.ExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 0:
            return this.precpred(this._ctx, 10);
         case 1:
            return this.precpred(this._ctx, 9);
         case 2:
            return this.precpred(this._ctx, 8);
         case 3:
            return this.precpred(this._ctx, 7);
         case 4:
            return this.precpred(this._ctx, 5);
         case 5:
            return this.precpred(this._ctx, 4);
         case 6:
            return this.precpred(this._ctx, 3);
         case 7:
            return this.precpred(this._ctx, 2);
         case 8:
            return this.precpred(this._ctx, 23);
         case 9:
            return this.precpred(this._ctx, 22);
         case 10:
            return this.precpred(this._ctx, 20);
         case 11:
            return this.precpred(this._ctx, 18);
         case 12:
            return this.precpred(this._ctx, 6);
         default:
            return true;
      }
   }

   static {
      RuntimeMetaData.checkVersion("4.13.1", "4.13.1");

      for (int i = 0; i < tokenNames.length; i++) {
         tokenNames[i] = VOCABULARY.getLiteralName(i);
         if (tokenNames[i] == null) {
            tokenNames[i] = VOCABULARY.getSymbolicName(i);
         }

         if (tokenNames[i] == null) {
            tokenNames[i] = "<INVALID>";
         }
      }

      _ATN = new ATNDeserializer()
         .deserialize(
            "\u0004\u00010Ë\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001(\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u00023\b\u0002\u0001\u0002\u0001\u0002\u0003\u00027\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0004\u0002j\b\u0002\u000b\u0002\f\u0002k\u0001\u0002\u0001\u0002\u0005\u0002p\b\u0002\n\u0002\f\u0002s\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002~\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u00ad\b\u0002\n\u0002\f\u0002°\t\u0002\u0001\u0003\u0001\u0003\u0003\u0003´\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004¹\b\u0004\n\u0004\f\u0004¼\t\u0004\u0001\u0005\u0003\u0005¿\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006Ä\b\u0006\n\u0006\f\u0006Ç\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0000\u0001\u0004\u0007\u0000\u0002\u0004\u0006\b\n\f\u0000\u0006\u0002\u0000\u0013\u0013\u0017\u0017\u0001\u0000\u0014\u0016\u0001\u0000\u0012\u0013\u0001\u0000!#\u0001\u0000$'\u0001\u0000()ð\u0000\u000e\u0001\u0000\u0000\u0000\u0002'\u0001\u0000\u0000\u0000\u0004}\u0001\u0000\u0000\u0000\u0006³\u0001\u0000\u0000\u0000\bµ\u0001\u0000\u0000\u0000\n¾\u0001\u0000\u0000\u0000\fÅ\u0001\u0000\u0000\u0000\u000e\u000f\u0003\u0002\u0001\u0000\u000f\u0010\u0005\u0000\u0000\u0001\u0010\u0001\u0001\u0000\u0000\u0000\u0011\u0012\u0003\u0004\u0002\u0000\u0012\u0013\u0005\u0018\u0000\u0000\u0013\u0014\u0003\u0006\u0003\u0000\u0014\u0015\u0005-\u0000\u0000\u0015\u0016\u0003\u0004\u0002\u0000\u0016(\u0001\u0000\u0000\u0000\u0017\u0018\u0003\u0004\u0002\u0000\u0018\u0019\u0005\u001c\u0000\u0000\u0019\u001a\u0003\u0004\u0002\u0000\u001a\u001b\u0005\u001d\u0000\u0000\u001b\u001c\u0005-\u0000\u0000\u001c\u001d\u0003\u0004\u0002\u0000\u001d(\u0001\u0000\u0000\u0000\u001e\u001f\u0003\u0006\u0003\u0000\u001f \u0005-\u0000\u0000 !\u0003\u0004\u0002\u0000!(\u0001\u0000\u0000\u0000\"#\u0005\t\u0000\u0000#(\u0003\u0004\u0002\u0000$%\u0005\n\u0000\u0000%(\u0003\u0004\u0002\u0000&(\u0003\u0004\u0002\u0000'\u0011\u0001\u0000\u0000\u0000'\u0017\u0001\u0000\u0000\u0000'\u001e\u0001\u0000\u0000\u0000'\"\u0001\u0000\u0000\u0000'$\u0001\u0000\u0000\u0000'&\u0001\u0000\u0000\u0000(\u0003\u0001\u0000\u0000\u0000)*\u0006\u0002\uffff\uffff\u0000*+\u0005 \u0000\u0000+,\u0005\u001a\u0000\u0000,-\u0003\u0004\u0002\u0000-.\u0005\u001b\u0000\u0000.~\u0001\u0000\u0000\u0000/~\u0005\u0004\u0000\u00000~\u0005\u000b\u0000\u000013\u0005\u0013\u0000\u000021\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u00004~\u0005\u0010\u0000\u000057\u0005\u0013\u0000\u000065\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u000078\u0001\u0000\u0000\u00008~\u0005\u0011\u0000\u00009~\u0005\u0007\u0000\u0000:~\u0005\b\u0000\u0000;~\u0005\u0003\u0000\u0000<~\u0005\u000f\u0000\u0000=>\u0003\b\u0004\u0000>?\u0005\u0018\u0000\u0000?@\u0005\r\u0000\u0000@~\u0001\u0000\u0000\u0000AB\u0005\f\u0000\u0000BC\u0005\u0018\u0000\u0000CD\u0003\u0006\u0003\u0000DE\u0005\u001a\u0000\u0000EF\u0003\n\u0005\u0000FG\u0005\u001b\u0000\u0000G~\u0001\u0000\u0000\u0000HI\u0003\u0006\u0003\u0000IJ\u0005\u001a\u0000\u0000JK\u0003\n\u0005\u0000KL\u0005\u001b\u0000\u0000L~\u0001\u0000\u0000\u0000MN\u0005.\u0000\u0000N~\u0003\u0006\u0003\u0000OP\u0003\u0006\u0003\u0000PQ\u0005.\u0000\u0000QR\u0005\u0005\u0000\u0000R~\u0001\u0000\u0000\u0000ST\u0007\u0000\u0000\u0000T~\u0003\u0004\u0002\u000fUV\u0005\u0005\u0000\u0000VW\u0003\u0006\u0003\u0000WX\u0005\u001a\u0000\u0000XY\u0003\n\u0005\u0000YZ\u0005\u001b\u0000\u0000Z~\u0001\u0000\u0000\u0000[\\\u0005\u0005\u0000\u0000\\]\u0003\b\u0004\u0000]^\u0005\u001c\u0000\u0000^_\u0005\u001d\u0000\u0000_`\u0005\u001e\u0000\u0000`a\u0003\f\u0006\u0000ab\u0005\u001f\u0000\u0000b~\u0001\u0000\u0000\u0000cd\u0005\u0005\u0000\u0000di\u0003\u0006\u0003\u0000ef\u0005\u001c\u0000\u0000fg\u0003\u0004\u0002\u0000gh\u0005\u001d\u0000\u0000hj\u0001\u0000\u0000\u0000ie\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000ki\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000lq\u0001\u0000\u0000\u0000mn\u0005\u001c\u0000\u0000np\u0005\u001d\u0000\u0000om\u0001\u0000\u0000\u0000ps\u0001\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000r~\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000tu\u0005\u001a\u0000\u0000uv\u0003\b\u0004\u0000vw\u0005\u001b\u0000\u0000wx\u0003\u0004\u0002\u000bx~\u0001\u0000\u0000\u0000yz\u0005\u001a\u0000\u0000z{\u0003\u0004\u0002\u0000{|\u0005\u001b\u0000\u0000|~\u0001\u0000\u0000\u0000})\u0001\u0000\u0000\u0000}/\u0001\u0000\u0000\u0000}0\u0001\u0000\u0000\u0000}2\u0001\u0000\u0000\u0000}6\u0001\u0000\u0000\u0000}9\u0001\u0000\u0000\u0000}:\u0001\u0000\u0000\u0000};\u0001\u0000\u0000\u0000}<\u0001\u0000\u0000\u0000}=\u0001\u0000\u0000\u0000}A\u0001\u0000\u0000\u0000}H\u0001\u0000\u0000\u0000}M\u0001\u0000\u0000\u0000}O\u0001\u0000\u0000\u0000}S\u0001\u0000\u0000\u0000}U\u0001\u0000\u0000\u0000}[\u0001\u0000\u0000\u0000}c\u0001\u0000\u0000\u0000}t\u0001\u0000\u0000\u0000}y\u0001\u0000\u0000\u0000~®\u0001\u0000\u0000\u0000\u007f\u0080\n\n\u0000\u0000\u0080\u0081\u0007\u0001\u0000\u0000\u0081\u00ad\u0003\u0004\u0002\u000b\u0082\u0083\n\t\u0000\u0000\u0083\u0084\u0007\u0002\u0000\u0000\u0084\u00ad\u0003\u0004\u0002\n\u0085\u0086\n\b\u0000\u0000\u0086\u0087\u0007\u0003\u0000\u0000\u0087\u00ad\u0003\u0004\u0002\t\u0088\u0089\n\u0007\u0000\u0000\u0089\u008a\u0007\u0004\u0000\u0000\u008a\u00ad\u0003\u0004\u0002\b\u008b\u008c\n\u0005\u0000\u0000\u008c\u008d\u0007\u0005\u0000\u0000\u008d\u00ad\u0003\u0004\u0002\u0006\u008e\u008f\n\u0004\u0000\u0000\u008f\u0090\u0005*\u0000\u0000\u0090\u00ad\u0003\u0004\u0002\u0005\u0091\u0092\n\u0003\u0000\u0000\u0092\u0093\u0005+\u0000\u0000\u0093\u00ad\u0003\u0004\u0002\u0004\u0094\u0095\n\u0002\u0000\u0000\u0095\u0096\u0005,\u0000\u0000\u0096\u00ad\u0003\u0004\u0002\u0003\u0097\u0098\n\u0017\u0000\u0000\u0098\u0099\u0005\u001c\u0000\u0000\u0099\u009a\u0003\u0004\u0002\u0000\u009a\u009b\u0005\u001d\u0000\u0000\u009b\u00ad\u0001\u0000\u0000\u0000\u009c\u009d\n\u0016\u0000\u0000\u009d\u009e\u0005\u0018\u0000\u0000\u009e\u00ad\u0003\u0006\u0003\u0000\u009f \n\u0014\u0000\u0000 ¡\u0005\u0018\u0000\u0000¡¢\u0003\u0006\u0003\u0000¢£\u0005\u001a\u0000\u0000£¤\u0003\n\u0005\u0000¤¥\u0005\u001b\u0000\u0000¥\u00ad\u0001\u0000\u0000\u0000¦§\n\u0012\u0000\u0000§¨\u0005.\u0000\u0000¨\u00ad\u0003\u0006\u0003\u0000©ª\n\u0006\u0000\u0000ª«\u0005\u0006\u0000\u0000«\u00ad\u0003\b\u0004\u0000¬\u007f\u0001\u0000\u0000\u0000¬\u0082\u0001\u0000\u0000\u0000¬\u0085\u0001\u0000\u0000\u0000¬\u0088\u0001\u0000\u0000\u0000¬\u008b\u0001\u0000\u0000\u0000¬\u008e\u0001\u0000\u0000\u0000¬\u0091\u0001\u0000\u0000\u0000¬\u0094\u0001\u0000\u0000\u0000¬\u0097\u0001\u0000\u0000\u0000¬\u009c\u0001\u0000\u0000\u0000¬\u009f\u0001\u0000\u0000\u0000¬¦\u0001\u0000\u0000\u0000¬©\u0001\u0000\u0000\u0000\u00ad°\u0001\u0000\u0000\u0000®¬\u0001\u0000\u0000\u0000®¯\u0001\u0000\u0000\u0000¯\u0005\u0001\u0000\u0000\u0000°®\u0001\u0000\u0000\u0000±´\u0005\u000f\u0000\u0000²´\u0005\u0004\u0000\u0000³±\u0001\u0000\u0000\u0000³²\u0001\u0000\u0000\u0000´\u0007\u0001\u0000\u0000\u0000µº\u0003\u0006\u0003\u0000¶·\u0005\u001c\u0000\u0000·¹\u0005\u001d\u0000\u0000¸¶\u0001\u0000\u0000\u0000¹¼\u0001\u0000\u0000\u0000º¸\u0001\u0000\u0000\u0000º»\u0001\u0000\u0000\u0000»\t\u0001\u0000\u0000\u0000¼º\u0001\u0000\u0000\u0000½¿\u0003\f\u0006\u0000¾½\u0001\u0000\u0000\u0000¾¿\u0001\u0000\u0000\u0000¿\u000b\u0001\u0000\u0000\u0000ÀÁ\u0003\u0004\u0002\u0000ÁÂ\u0005\u0019\u0000\u0000ÂÄ\u0001\u0000\u0000\u0000ÃÀ\u0001\u0000\u0000\u0000ÄÇ\u0001\u0000\u0000\u0000ÅÃ\u0001\u0000\u0000\u0000ÅÆ\u0001\u0000\u0000\u0000ÆÈ\u0001\u0000\u0000\u0000ÇÅ\u0001\u0000\u0000\u0000ÈÉ\u0003\u0004\u0002\u0000É\r\u0001\u0000\u0000\u0000\f'26kq}¬®³º¾Å"
               .toCharArray()
         );
      _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];

      for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
         _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
      }
   }

   public static class AdditiveExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public Token op;
      public ExpressionParser.ExpressionContext right;

      public AdditiveExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterAdditiveExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitAdditiveExpression(this);
         }
      }
   }

   public static class ArgumentsContext extends ParserRuleContext {
      public ExpressionParser.NonEmptyArgumentsContext nonEmptyArguments() {
         return this.getRuleContext(ExpressionParser.NonEmptyArgumentsContext.class, 0);
      }

      public ArgumentsContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 5;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterArguments(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitArguments(this);
         }
      }
   }

   public static class ArrayAccessExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext arr;
      public ExpressionParser.ExpressionContext index;

      public ArrayAccessExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterArrayAccessExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitArrayAccessExpression(this);
         }
      }
   }

   public static class ArrayLitExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameWithDimsContext elementType;
      public ExpressionParser.NonEmptyArgumentsContext values;

      public ArrayLitExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterArrayLitExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitArrayLitExpression(this);
         }
      }
   }

   public static class ArrayStoreStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.ExpressionContext arr;
      public ExpressionParser.ExpressionContext index;
      public ExpressionParser.ExpressionContext value;

      public ArrayStoreStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterArrayStoreStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitArrayStoreStatement(this);
         }
      }
   }

   public static class BitwiseAndExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public ExpressionParser.ExpressionContext right;

      public BitwiseAndExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterBitwiseAndExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitBitwiseAndExpression(this);
         }
      }
   }

   public static class BitwiseOrExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public ExpressionParser.ExpressionContext right;

      public BitwiseOrExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterBitwiseOrExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitBitwiseOrExpression(this);
         }
      }
   }

   public static class BitwiseXorExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public ExpressionParser.ExpressionContext right;

      public BitwiseXorExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterBitwiseXorExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitBitwiseXorExpression(this);
         }
      }
   }

   public static class BoolLitExpressionContext extends ExpressionParser.ExpressionContext {
      public Token lit;

      public BoolLitExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterBoolLitExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitBoolLitExpression(this);
         }
      }
   }

   public static class BoundMethodReferenceExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext receiver;
      public ExpressionParser.NameContext memberName;

      public BoundMethodReferenceExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterBoundMethodReferenceExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitBoundMethodReferenceExpression(this);
         }
      }
   }

   public static class CapturingExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext expr;

      public CapturingExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterCapturingExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitCapturingExpression(this);
         }
      }
   }

   public static class CastExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameWithDimsContext type;
      public ExpressionParser.ExpressionContext expr;

      public CastExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterCastExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitCastExpression(this);
         }
      }
   }

   public static class ClassConstantExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameWithDimsContext type;

      public ClassConstantExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterClassConstantExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitClassConstantExpression(this);
         }
      }
   }

   public static class ComparisonExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public Token op;
      public ExpressionParser.ExpressionContext right;

      public ComparisonExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterComparisonExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitComparisonExpression(this);
         }
      }
   }

   public static class ConstructorReferenceExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext type;

      public ConstructorReferenceExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterConstructorReferenceExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitConstructorReferenceExpression(this);
         }
      }
   }

   public static class DecimalLitExpressionContext extends ExpressionParser.ExpressionContext {
      public Token lit;

      public DecimalLitExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterDecimalLitExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitDecimalLitExpression(this);
         }
      }
   }

   public static class EqualityExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public Token op;
      public ExpressionParser.ExpressionContext right;

      public EqualityExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterEqualityExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitEqualityExpression(this);
         }
      }
   }

   public static class ExpressionContext extends ParserRuleContext {
      public ExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 2;
      }

      public ExpressionContext() {
      }

      public void copyFrom(ExpressionParser.ExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ExpressionStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public ExpressionStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterExpressionStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitExpressionStatement(this);
         }
      }
   }

   public static class FreeMethodReferenceExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext memberName;

      public FreeMethodReferenceExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterFreeMethodReferenceExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitFreeMethodReferenceExpression(this);
         }
      }
   }

   public static class IdentifierAssignmentStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.NameContext identifier;
      public ExpressionParser.ExpressionContext value;

      public IdentifierAssignmentStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterIdentifierAssignmentStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitIdentifierAssignmentStatement(this);
         }
      }
   }

   public static class IdentifierExpressionContext extends ExpressionParser.ExpressionContext {
      public Token id;

      public IdentifierExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterIdentifierExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitIdentifierExpression(this);
         }
      }
   }

   public static class IdentifierNameContext extends ExpressionParser.NameContext {
      public IdentifierNameContext(ExpressionParser.NameContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterIdentifierName(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitIdentifierName(this);
         }
      }
   }

   public static class InstanceofExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext expr;
      public ExpressionParser.NameWithDimsContext type;

      public InstanceofExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterInstanceofExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitInstanceofExpression(this);
         }
      }
   }

   public static class InstantiationExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext type;
      public ExpressionParser.ArgumentsContext args;

      public InstantiationExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterInstantiationExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitInstantiationExpression(this);
         }
      }
   }

   public static class IntLitExpressionContext extends ExpressionParser.ExpressionContext {
      public Token lit;

      public IntLitExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterIntLitExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitIntLitExpression(this);
         }
      }
   }

   public static class MemberAccessExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext receiver;
      public ExpressionParser.NameContext memberName;

      public MemberAccessExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterMemberAccessExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitMemberAccessExpression(this);
         }
      }
   }

   public static class MemberAssignmentStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.ExpressionContext receiver;
      public ExpressionParser.NameContext memberName;
      public ExpressionParser.ExpressionContext value;

      public MemberAssignmentStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterMemberAssignmentStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitMemberAssignmentStatement(this);
         }
      }
   }

   public static class MethodCallExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext receiver;
      public ExpressionParser.NameContext memberName;
      public ExpressionParser.ArgumentsContext args;

      public MethodCallExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterMethodCallExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitMethodCallExpression(this);
         }
      }
   }

   public static class MultiplicativeExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public Token op;
      public ExpressionParser.ExpressionContext right;

      public MultiplicativeExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterMultiplicativeExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitMultiplicativeExpression(this);
         }
      }
   }

   public static class NameContext extends ParserRuleContext {
      public NameContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 3;
      }

      public NameContext() {
      }

      public void copyFrom(ExpressionParser.NameContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class NameWithDimsContext extends ParserRuleContext {
      public Token LeftBracket;
      public List<Token> dims = new ArrayList<>();

      public ExpressionParser.NameContext name() {
         return this.getRuleContext(ExpressionParser.NameContext.class, 0);
      }

      public NameWithDimsContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 4;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterNameWithDims(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitNameWithDims(this);
         }
      }
   }

   public static class NewArrayExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext innerType;
      public ExpressionParser.ExpressionContext expression;
      public List<ExpressionParser.ExpressionContext> dims = new ArrayList<>();
      public Token LeftBracket;
      public List<Token> blankDims = new ArrayList<>();

      public NewArrayExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterNewArrayExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitNewArrayExpression(this);
         }
      }
   }

   public static class NonEmptyArgumentsContext extends ParserRuleContext {
      public List<ExpressionParser.ExpressionContext> expression() {
         return this.getRuleContexts(ExpressionParser.ExpressionContext.class);
      }

      public NonEmptyArgumentsContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 6;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterNonEmptyArguments(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitNonEmptyArguments(this);
         }
      }
   }

   public static class NullExpressionContext extends ExpressionParser.ExpressionContext {
      public Token lit;

      public NullExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterNullExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitNullExpression(this);
         }
      }
   }

   public static class ParenthesizedExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext expr;

      public ParenthesizedExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterParenthesizedExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitParenthesizedExpression(this);
         }
      }
   }

   public static class ReturnStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.ExpressionContext value;

      public ReturnStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterReturnStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitReturnStatement(this);
         }
      }
   }

   public static class RootContext extends ParserRuleContext {
      public ExpressionParser.StatementContext statement() {
         return this.getRuleContext(ExpressionParser.StatementContext.class, 0);
      }

      public RootContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 0;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterRoot(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitRoot(this);
         }
      }
   }

   public static class ShiftExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.ExpressionContext left;
      public Token op;
      public ExpressionParser.ExpressionContext right;

      public ShiftExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterShiftExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitShiftExpression(this);
         }
      }
   }

   public static class StatementContext extends ParserRuleContext {
      public StatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 1;
      }

      public StatementContext() {
      }

      public void copyFrom(ExpressionParser.StatementContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class StaticMethodCallExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext memberName;
      public ExpressionParser.ArgumentsContext args;

      public StaticMethodCallExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterStaticMethodCallExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitStaticMethodCallExpression(this);
         }
      }
   }

   public static class StringLitExpressionContext extends ExpressionParser.ExpressionContext {
      public Token lit;

      public StringLitExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterStringLitExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitStringLitExpression(this);
         }
      }
   }

   public static class SuperCallExpressionContext extends ExpressionParser.ExpressionContext {
      public ExpressionParser.NameContext memberName;
      public ExpressionParser.ArgumentsContext args;

      public SuperCallExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterSuperCallExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitSuperCallExpression(this);
         }
      }
   }

   public static class ThisExpressionContext extends ExpressionParser.ExpressionContext {
      public ThisExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterThisExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitThisExpression(this);
         }
      }
   }

   public static class ThrowStatementContext extends ExpressionParser.StatementContext {
      public ExpressionParser.ExpressionContext value;

      public ThrowStatementContext(ExpressionParser.StatementContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterThrowStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitThrowStatement(this);
         }
      }
   }

   public static class UnaryExpressionContext extends ExpressionParser.ExpressionContext {
      public Token op;
      public ExpressionParser.ExpressionContext expr;

      public UnaryExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterUnaryExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitUnaryExpression(this);
         }
      }
   }

   public static class WildcardExpressionContext extends ExpressionParser.ExpressionContext {
      public WildcardExpressionContext(ExpressionParser.ExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterWildcardExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitWildcardExpression(this);
         }
      }
   }

   public static class WildcardNameContext extends ExpressionParser.NameContext {
      public WildcardNameContext(ExpressionParser.NameContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).enterWildcardName(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionParserListener) {
            ((ExpressionParserListener)listener).exitWildcardName(this);
         }
      }
   }
}
