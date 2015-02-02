(function() {
    "use strict";

    var module = angular.module('dice-game');

    module.service('commandService', function($http) {
        return {
            createGame: function(success, error) {
                $http.post('/game')
                    .success(success)
                    .error(error);
            },
            startGame: function(gameId, playersCount, error) {
                $http.post('/game/' + gameId + '/start?playersCount=' + playersCount)
                    .error(error);
            },
            roll: function(gameId, player, error) {
                $http.post('/game/' + gameId + '/roll/' + player)
                    .error(error);
            }
        };
    });

})();
