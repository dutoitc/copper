'use strict';

angular.module('copperApp.story', ['ngRoute'])
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/story/:pStoryName', {
    templateUrl: 'app/views/story.html',
    controller: 'StoryCtrl'
  });
}])

.controller('StoryCtrl', ['$http', '$scope', '$routeParams', function($http, $scope, $routeParams) {
    var scope = $scope;
    var self=this;
    var originalStoryName = $routeParams.pStoryName;


    $http.get('ws/story/'+originalStoryName)
        .success(function(data) {
            console.log(data);
            $scope.story=data[0];
            $scope.dirty=false;
    });

    $scope.submit = function() {
        var data = JSON.stringify({
                        //json: JSON.stringify($scope.story)
                        originalStoryName: originalStoryName,
                        storyName: $scope.story.name,
                        cron: $scope.story.cron,
                        storyText: $scope.story.storyText
                    });

        $http.post('ws/story/'+originalStoryName, data)
            .then(
                function(data, status) {
                    if (data.data=="Ok") {
                        $scope.message = "The story has been saved.";
                        $scope.dirty=false;
                    } else {
                        $scope.message = "Unknown return: " + data;
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