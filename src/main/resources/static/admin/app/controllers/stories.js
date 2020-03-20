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
    $scope.sortBy='story.name';
    $scope.sortReverse=false;


    $scope.triggerSort = function(value) {
        if (value==$scope.sortBy) {
            $scope.sortReverse = !$scope.sortReverse;
        } else {
            $scope.sortReverse = false;
            $scope.sortBy = value;
        }
    }

    $scope.refreshStories = function() {
        $http.get('../ws/admin/stories')
            .then(function(response) {
                $scope.stories=response.data;
        });
    }

    $scope.runStory = function(storyName) {
        $http.get('../ws/admin/story/' + storyName + '/run')
                .then(function(response) {
                    alert(response.data);
            });
    }

    $scope.deleteStory = function(storyName) {
        if ( window.confirm("Delete story " + storyName + " ?") ) {
            $http.get('../ws/admin/story/' + storyName + '/delete')
                    .then(function(response) {
                        alert(response.data);
                });
            $scope.refreshStories();
        }
    }

    $scope.refreshStories();

}]);