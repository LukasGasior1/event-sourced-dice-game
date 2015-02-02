(function () {
    "use strict";

    var module = angular.module('dice-game');

    module.controller('StartGameController', function($scope, $rootScope, commandService) {

        $scope.playersCount = 6;

        $scope.loading = false;

        $scope.hidePopoverTimeout = null;

        $scope.startGame = function() {
            $scope.loading = true;
            commandService.startGame($scope.gameId, $scope.playersCount, function(data) {
                $scope.loading = false;
                if (data.message) {
                    if ($scope.hidePopoverTimeout != null) {
                        clearTimeout($scope.hidePopoverTimeout);
                    }
                    $('#start-game').popover({
                        content: data.message,
                        placement: 'left',
                        trigger: 'manual'
                    }).popover('show');
                    $scope.hidePopoverTimeout = setTimeout(function() {
                        $('#start-game').popover('hide');
                    }, 1500);
                } else {
                    alert('Unexpected error occurred, try again later.');
                }
            });
        };

    });

})();
