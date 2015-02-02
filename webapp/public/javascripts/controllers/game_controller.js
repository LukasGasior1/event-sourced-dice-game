(function () {
    "use strict";

    var module = angular.module('dice-game');

    module.controller('GameController', function($scope, $rootScope, commandService) {

        $scope.selectedPlayer = $rootScope.game.players[0];

        $scope.winners = [];

        $scope.gameFinished = false;

        $scope.playAgain = function() {
            $rootScope.gameId = null;
            $rootScope.game = null;
            $rootScope.page = "create";
        };

        $scope.hidePopoverTimeout = null;

        $scope.roll = function() {
            commandService.roll($scope.gameId, $scope.selectedPlayer, function(data) {
                if (data.message) {
                    if ($scope.hidePopoverTimeout != null) {
                        clearTimeout($scope.hidePopoverTimeout);
                    }
                    $('#roll').popover({
                        content: data.message,
                        placement: 'left',
                        trigger: 'manual'
                    }).popover('show');
                    $scope.hidePopoverTimeout = setTimeout(function() {
                        $('#roll').popover('hide');
                    }, 1500);
                } else {
                    alert('Unexpected error occurred, try again later.');
                }
            });
        };

        $scope.isWinner = function(player) {
            return $.inArray(player, $scope.winners) != -1;
        };

        $rootScope.$on('events.TurnChanged', function(event, data) {
            $scope.selectedPlayer = data.turn.currentPlayer;
            $scope.$apply();
        });

        $rootScope.$on('events.GameFinished', function(event, data) {
            $scope.gameFinished = true;
            $scope.winners = data.winners;
            $scope.$apply();
        });

    });

})();
