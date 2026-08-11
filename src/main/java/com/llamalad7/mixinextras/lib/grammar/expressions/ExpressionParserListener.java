package com.llamalad7.mixinextras.lib.grammar.expressions;

import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ParseTreeListener;

public interface ExpressionParserListener extends ParseTreeListener {
   void enterRoot(ExpressionParser.RootContext var1);

   void exitRoot(ExpressionParser.RootContext var1);

   void enterMemberAssignmentStatement(ExpressionParser.MemberAssignmentStatementContext var1);

   void exitMemberAssignmentStatement(ExpressionParser.MemberAssignmentStatementContext var1);

   void enterArrayStoreStatement(ExpressionParser.ArrayStoreStatementContext var1);

   void exitArrayStoreStatement(ExpressionParser.ArrayStoreStatementContext var1);

   void enterIdentifierAssignmentStatement(ExpressionParser.IdentifierAssignmentStatementContext var1);

   void exitIdentifierAssignmentStatement(ExpressionParser.IdentifierAssignmentStatementContext var1);

   void enterReturnStatement(ExpressionParser.ReturnStatementContext var1);

   void exitReturnStatement(ExpressionParser.ReturnStatementContext var1);

   void enterThrowStatement(ExpressionParser.ThrowStatementContext var1);

   void exitThrowStatement(ExpressionParser.ThrowStatementContext var1);

   void enterExpressionStatement(ExpressionParser.ExpressionStatementContext var1);

   void exitExpressionStatement(ExpressionParser.ExpressionStatementContext var1);

   void enterBitwiseXorExpression(ExpressionParser.BitwiseXorExpressionContext var1);

   void exitBitwiseXorExpression(ExpressionParser.BitwiseXorExpressionContext var1);

   void enterClassConstantExpression(ExpressionParser.ClassConstantExpressionContext var1);

   void exitClassConstantExpression(ExpressionParser.ClassConstantExpressionContext var1);

   void enterStaticMethodCallExpression(ExpressionParser.StaticMethodCallExpressionContext var1);

   void exitStaticMethodCallExpression(ExpressionParser.StaticMethodCallExpressionContext var1);

   void enterBoolLitExpression(ExpressionParser.BoolLitExpressionContext var1);

   void exitBoolLitExpression(ExpressionParser.BoolLitExpressionContext var1);

   void enterUnaryExpression(ExpressionParser.UnaryExpressionContext var1);

   void exitUnaryExpression(ExpressionParser.UnaryExpressionContext var1);

   void enterFreeMethodReferenceExpression(ExpressionParser.FreeMethodReferenceExpressionContext var1);

   void exitFreeMethodReferenceExpression(ExpressionParser.FreeMethodReferenceExpressionContext var1);

   void enterConstructorReferenceExpression(ExpressionParser.ConstructorReferenceExpressionContext var1);

   void exitConstructorReferenceExpression(ExpressionParser.ConstructorReferenceExpressionContext var1);

   void enterInstantiationExpression(ExpressionParser.InstantiationExpressionContext var1);

   void exitInstantiationExpression(ExpressionParser.InstantiationExpressionContext var1);

   void enterIntLitExpression(ExpressionParser.IntLitExpressionContext var1);

   void exitIntLitExpression(ExpressionParser.IntLitExpressionContext var1);

   void enterThisExpression(ExpressionParser.ThisExpressionContext var1);

   void exitThisExpression(ExpressionParser.ThisExpressionContext var1);

   void enterDecimalLitExpression(ExpressionParser.DecimalLitExpressionContext var1);

   void exitDecimalLitExpression(ExpressionParser.DecimalLitExpressionContext var1);

   void enterMethodCallExpression(ExpressionParser.MethodCallExpressionContext var1);

   void exitMethodCallExpression(ExpressionParser.MethodCallExpressionContext var1);

   void enterInstanceofExpression(ExpressionParser.InstanceofExpressionContext var1);

   void exitInstanceofExpression(ExpressionParser.InstanceofExpressionContext var1);

   void enterWildcardExpression(ExpressionParser.WildcardExpressionContext var1);

   void exitWildcardExpression(ExpressionParser.WildcardExpressionContext var1);

   void enterArrayLitExpression(ExpressionParser.ArrayLitExpressionContext var1);

   void exitArrayLitExpression(ExpressionParser.ArrayLitExpressionContext var1);

   void enterStringLitExpression(ExpressionParser.StringLitExpressionContext var1);

   void exitStringLitExpression(ExpressionParser.StringLitExpressionContext var1);

   void enterEqualityExpression(ExpressionParser.EqualityExpressionContext var1);

   void exitEqualityExpression(ExpressionParser.EqualityExpressionContext var1);

   void enterMultiplicativeExpression(ExpressionParser.MultiplicativeExpressionContext var1);

   void exitMultiplicativeExpression(ExpressionParser.MultiplicativeExpressionContext var1);

   void enterBitwiseOrExpression(ExpressionParser.BitwiseOrExpressionContext var1);

   void exitBitwiseOrExpression(ExpressionParser.BitwiseOrExpressionContext var1);

   void enterParenthesizedExpression(ExpressionParser.ParenthesizedExpressionContext var1);

   void exitParenthesizedExpression(ExpressionParser.ParenthesizedExpressionContext var1);

   void enterAdditiveExpression(ExpressionParser.AdditiveExpressionContext var1);

   void exitAdditiveExpression(ExpressionParser.AdditiveExpressionContext var1);

   void enterMemberAccessExpression(ExpressionParser.MemberAccessExpressionContext var1);

   void exitMemberAccessExpression(ExpressionParser.MemberAccessExpressionContext var1);

   void enterBoundMethodReferenceExpression(ExpressionParser.BoundMethodReferenceExpressionContext var1);

   void exitBoundMethodReferenceExpression(ExpressionParser.BoundMethodReferenceExpressionContext var1);

   void enterShiftExpression(ExpressionParser.ShiftExpressionContext var1);

   void exitShiftExpression(ExpressionParser.ShiftExpressionContext var1);

   void enterCapturingExpression(ExpressionParser.CapturingExpressionContext var1);

   void exitCapturingExpression(ExpressionParser.CapturingExpressionContext var1);

   void enterNullExpression(ExpressionParser.NullExpressionContext var1);

   void exitNullExpression(ExpressionParser.NullExpressionContext var1);

   void enterIdentifierExpression(ExpressionParser.IdentifierExpressionContext var1);

   void exitIdentifierExpression(ExpressionParser.IdentifierExpressionContext var1);

   void enterBitwiseAndExpression(ExpressionParser.BitwiseAndExpressionContext var1);

   void exitBitwiseAndExpression(ExpressionParser.BitwiseAndExpressionContext var1);

   void enterComparisonExpression(ExpressionParser.ComparisonExpressionContext var1);

   void exitComparisonExpression(ExpressionParser.ComparisonExpressionContext var1);

   void enterSuperCallExpression(ExpressionParser.SuperCallExpressionContext var1);

   void exitSuperCallExpression(ExpressionParser.SuperCallExpressionContext var1);

   void enterCastExpression(ExpressionParser.CastExpressionContext var1);

   void exitCastExpression(ExpressionParser.CastExpressionContext var1);

   void enterNewArrayExpression(ExpressionParser.NewArrayExpressionContext var1);

   void exitNewArrayExpression(ExpressionParser.NewArrayExpressionContext var1);

   void enterArrayAccessExpression(ExpressionParser.ArrayAccessExpressionContext var1);

   void exitArrayAccessExpression(ExpressionParser.ArrayAccessExpressionContext var1);

   void enterIdentifierName(ExpressionParser.IdentifierNameContext var1);

   void exitIdentifierName(ExpressionParser.IdentifierNameContext var1);

   void enterWildcardName(ExpressionParser.WildcardNameContext var1);

   void exitWildcardName(ExpressionParser.WildcardNameContext var1);

   void enterNameWithDims(ExpressionParser.NameWithDimsContext var1);

   void exitNameWithDims(ExpressionParser.NameWithDimsContext var1);

   void enterArguments(ExpressionParser.ArgumentsContext var1);

   void exitArguments(ExpressionParser.ArgumentsContext var1);

   void enterNonEmptyArguments(ExpressionParser.NonEmptyArgumentsContext var1);

   void exitNonEmptyArguments(ExpressionParser.NonEmptyArgumentsContext var1);
}
