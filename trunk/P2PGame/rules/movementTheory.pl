latitude(L):-current_positionX(Xi),target_positionX(Xf),L is Xf-Xi.
longitude(L):-current_positionY(Yi),target_positionY(Yf),L is Yf-Yi.
relative_positionX(P) :-latitude(L) ,(( L < 0 , P is -1); (L = 0, P=0);(L>0,P is 1)).
relative_positionY(P) :-longitude(L) ,(( L < 0 , P is -1); (L = 0, P=0);(L>0,P is 1)).

%NORTH
north:- relative_positionX(-1), visibility([_,0,_,_,_,_,_,_]).
north:-relative_positionX(0),((relative_positionY(1),visibility([_,_,_,1,_,_,_,_]));(relative_positionY(-1),visibility([_,_,_,_,1,_,_,_]))),((previous_movementX(-1));(previous_movementX(1), visibility([_,_,_,_,_,_,1,_]))).
north:-relative_positionX(1),visibility([_,0,_,1,1,_,1,_]).

%SOUTH
south:- relative_positionX(1), visibility([_,_,_,_,_,_,0,_]).
south:-relative_positionX(0),((relative_positionY(1),visibility([_,_,_,1,_,_,_,_]));(relative_positionY(-1),visibility([_,_,_,_,1,_,_,_]))),((previous_movementX(1));(previous_movementX(-1), visibility([_,1,_,_,_,_,_,_]))).
south:-relative_positionX(-1),visibility([_,1,_,1,1,_,0,_]).

%WEST
west:- relative_positionY(-1), visibility([_,_,_,0,_,_,_,_]).
west:-relative_positionY(0),((relative_positionX(1),visibility([_,1,_,_,_,_,_,_]));(relative_positionX(-1),visibility([_,_,_,_,_,_,1,_]))),((previous_movementY(-1));(previous_movementY(1), visibility([_,_,_,_,1,_,_,_]))).
west:-relative_positionY(1),visibility([_,1,_,0,1,_,1,_]).

%EAST
east:- relative_positionY(1), visibility([_,_,_,_,0,_,_,_]).
east:-relative_positionY(0),((relative_positionX(1),visibility([_,1,_,_,_,_,_,_]));(relative_positionX(-1),visibility([_,_,_,_,_,_,1,_]))),((previous_movementY(1));(previous_movementY(-1), visibility([_,_,_,1,_,_,_,_]))).
east:-relative_positionY(-1),visibility([_,1,_,1,0,_,1,_]).

%decide movement

movement(X):-(north, X is 1,!);(south, X is 2,!);(east,X is 3, !);(west,X is 4,!).