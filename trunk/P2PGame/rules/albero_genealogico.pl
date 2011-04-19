genitore(pam,bob).
genitore(tom,bob).
genitore(tom,liz).
genitore(bob,ann).
genitore(bob, pat).
genitore(pat,jim).

femmina(pam).
femmina(liz).
femmina(ann).
maschio(bob).
maschio(tom).
femmina(pat).
maschio(jim).

figlio(X,Y):- genitore(Y,X),maschio(X). 
figlia(X,Y):- genitore(Y,X),femmina(X). 
padre(X,Y):- genitore(X,Y),maschio(X).
madre(X,Y):-genitore(X,Y),femmina(X).
nonno(X,Y):- genitore(X,Z),genitore(Z,Y),maschio(X).
nonna(X,Y):- genitore(X,Z),genitore(Z,Y),femmina(X).
fratello(X,Y):-genitore(Z,X),genitore(Z,Y),maschio(X).
sorella(X,Y):-genitore(Z,X),genitore(Z,Y),femmina(X).
predecessore(X,Y):-genitore(X,Y).
predecessore(X,Y):-genitore(X,Z),predecessore(Z,Y).