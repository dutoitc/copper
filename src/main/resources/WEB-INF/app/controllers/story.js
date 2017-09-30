'use strict';

angular.module('copperApp.story', ['ngRoute'])
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/story/:pStoryName', {
    templateUrl: 'app/views/story.html',
    controller: 'StoryCtrl'
  });
}])

.controller('StoryCtrl', ['$http', '$scope', '$routeParams', '$sce', function($http, $scope, $routeParams, $sce) {
    var scope = $scope;
    var self=this;
    $scope.originalStoryName = $routeParams.pStoryName;
    $scope.redirectToStories=false;



    if ($scope.originalStoryName=='new') {
        $scope.story = {
            name:'new',
            storyText:'GIVEN ...\nWHEN ...\nTHEN ...\n'
        }
    } else {
        $http.get('ws/story/'+$scope.originalStoryName)
                .then(function(response) {
                    $scope.story=response.data;
            });
    }

    $scope.submit = function() {
        var data = JSON.stringify({
                        //json: JSON.stringify($scope.story)
                        originalStoryName: $scope.originalStoryName,
                        storyName: $scope.story.name,
                        storyText: $scope.story.storyText
                    });

        $http.post('ws/story/'+$scope.originalStoryName, data)
            .then(
                function(data, status) {
                    if (data.data=="Ok") {
                        $scope.message = $sce.trustAsHtml("The story has been saved.");
                        $scope.redirectToStories=true;
                    } else {
                        $scope.message = $sce.trustAsHtml("Unknown return: <pre>" + data.data + "</pre>");
                    }
                },
                function(data, status) {
                    $scope.error="Cannot save: " + data;
                });
    };


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