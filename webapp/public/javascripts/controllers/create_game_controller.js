(function () {
    "use strict";

    var module = angular.module('dice-game');

    module.controller('CreateGameController', function($scope, $rootScope, commandService, eventService) {

        $scope.loading = false;

        $scope.createNewGame = function() {
            $scope.loading = true;
            commandService.createGame(function(data) {
                $scope.loading = false;
                $rootScope.gameId = data.id;
                $rootScope.page = "choose_players";
                eventService.connect($scope.gameId);
            }, function(data) {
                $scope.loading = false;
                alert('Unexpected error occurred, try again later.');
            });
        };

    });

})();
