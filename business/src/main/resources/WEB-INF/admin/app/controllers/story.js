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
    $scope.errors=null;



    if ($scope.originalStoryName=='new') {
        $scope.story = {
            name:'new',
            storyText:'GIVEN ...\nWHEN ...\nTHEN ...\n'
        }
    } else {
        $http.get('/ws/story/'+$scope.originalStoryName)
                .then(function(response) {
                    $scope.story=response.data;
            });
    }

    $scope.validate = function() {
        $http.post('/ws/validation/story', $scope.story.storyText)
            .then(
                function(data, status) {
                    console.log(data.data);
                    $scope.validation = data.data;
                    $scope.validationHTML = $sce.trustAsHtml(buildValidation(data.data));
                    console.log("validationHTML", $scope.validationHTML);
                },
                function(data, status) {
                    $scope.errors="Cannot validate: " + data;
                });
        };

     function buildValidation(validationResult) {
        console.log("ValidationResult", validationResult);
        if (validationResult.perfectMatches.length>0) return "<span style='color:green'>Valid</span>";

        // Init array of validation: 0=invalid, 1=valid
        var s = [];
        for (var i=0; i<validationResult.story.length; i++) {
            s.push(0);
        }
        for (var i=0; i<validationResult.partialMatches.length; i++) {
            var matchedPattern = validationResult.partialMatches[i];
            for (var j=matchedPattern.start; j<=matchedPattern.end; j++) {
                s[j]=1;
            }
        }

        // HTML
        var html="<span style='color:red;font-weight:bolder'>";
        var current = -1;
        for (var i=0; i<validationResult.story.length; i++) {
            var news = s[i];
            if (news==1 && news!=current) {
                html+="<span style='color:green;font-weight:bolder'>";
            }
            if (validationResult.story[i]=='\n') {
                html=html+'<br/>';
            } else {
                html+=validationResult.story[i];
            }
            if (news==0 && news!=current) {
                html+="</span>";
            }
            current = news;
        }
        if (current==1) html+="</span>";
        html+='</span><br/><br/>';

        // Matching patterns
        if (validationResult.partialMatches.length>0) {
            html=html+'<b>Matched patterns</b><br/>';
            for (var i=0; i<validationResult.partialMatches.length; i++) {
                var matchedPattern = validationResult.partialMatches[i];
                html+=matchedPattern.patternName+'<ul>';
                html+='<li>patternShort: ' + matchedPattern.patternShort + '</li>';
                html+='<li>patternFull: ' + matchedPattern.patternFull + '</li>';
                html+='<li>Matched: ' + validationResult.story.substring(matchedPattern.start, matchedPattern.end) + '</li>';
                html+='</ul>';
            }
        }

        console.log("html", html);
        return html;
     }

    $scope.submit = function() {
         $scope.errors=null;
        var data = JSON.stringify({
                        //json: JSON.stringify($scope.story)
                        originalStoryName: $scope.originalStoryName,
                        storyName: $scope.story.name,
                        storyText: $scope.story.storyText
                    });

        $http.post('/ws/story/'+$scope.originalStoryName, data)
            .then(
                function(data, status) {
                    if (data.data=="Ok") {
                        $scope.message = $sce.trustAsHtml("The story has been saved.");
                        $scope.redirectToStories=true;
                    } else {
                        $scope.message = $sce.trustAsHtml("<pre>" + data.data + "</pre>");
                    }
                },
                function(data, status) {
                    $scope.errors="Cannot save story: " + data.data;
                    console.log("Cannot save story", data);
                });
    };


}]);