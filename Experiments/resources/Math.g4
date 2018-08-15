grammar Math;
    main: Expr;
    Expr: NUMBER Expr1 ;
    Expr1: Expr OP Expr | EOF ;
    OP: [x+-/] ;
    NUMBER: [0-9]+ ;
    WS: [ \t\r\n]+ -> skip ;
