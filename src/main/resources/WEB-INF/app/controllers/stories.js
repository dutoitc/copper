'use strict';

angular.module('copperApp.stories', ['ngRoute'])
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/stories', {
    templateUrl: 'app/views/stories.html',
    controller: 'StoriesCtrl'
  });
}])

.controller('StoriesCtrl', ['$http', '$scope', function($http, $scope) {
    var scope = $scope;
    var self=this;


    $scope.refreshStories = function() {
        $http.get('ws/stories')
            .success(function(data) {
                $scope.stories=data;
        });
    }

    $scope.runStory = function(storyName) {
        $http.get('/ws/story/' + storyName + '/run')
                .success(function(data) {
                    alert(data);
            });
    }

    $scope.deleteStory = function(storyName) {
        if ( window.confirm("Delete story " + storyName + " ?") ) {
            $http.get('/ws/story/' + storyName + '/delete')
                    .success(function(data) {
                        alert(data);
                });
            $scope.refreshStories();
        }
    }

    $scope.refreshStories();


/*

    $http.get('data/routes.json')
        .success(function(data) {
            $scope.routes=data["routes"];
    });
    $http.get('data/processes.json')
        .success(function(data) {
            $scope.processes=data["processes"];
    });
    $http.get('data/services.json')
        .success(function(data) {
            $scope.services=data["services"];
    });*/

}]);