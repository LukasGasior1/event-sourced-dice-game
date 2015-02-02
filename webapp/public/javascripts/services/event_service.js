(function() {
    "use strict";
    
    var module = angular.module('dice-game');

    module.service('eventService', function($rootScope) {
        var ws = null;
        return {
            connect: function(gameId) {
                ws = new WebSocket('ws://' + window.location.hostname + ':' + window.location.port + '/' + gameId + '/events');
                ws.onmessage = function(event) {
                    var message = JSON.parse(event.data);
                    $rootScope.$broadcast('events.' + message.eventType, message.data);
                };
            },
            disconnect: function() {
                if (ws != null) {
                    ws.close();
                }
            }
        };
    });

})();
