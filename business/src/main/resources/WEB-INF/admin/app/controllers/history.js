'use strict';

angular.module('copperApp.history', ['ngRoute'])
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/history', {
    templateUrl: 'app/views/history.html',
    controller: 'historyCtrl'
  });
}])

.controller('historyCtrl', ['$http', '$scope', '$routeParams', function($http, $scope, $routeParams) {
    var scope = $scope;
    var self=this;
    var key = $routeParams.key;

    $scope.labels = [];
    $scope.series = [];
    $scope.data = [];
    $scope.onClick = function (points, evt) {
      console.log(points, evt);
    };
    $scope.datasetOverride = [{ yAxisID: 'y-axis-1' }, { yAxisID: 'y-axis-2' }];
    $scope.options = {
      scales: {
        yAxes: [
          {
            id: 'y-axis-1',
            type: 'linear',
            display: true,
            position: 'left'
          },
          {
            id: 'y-axis-2',
            type: 'linear',
            display: true,
            position: 'right'
          }
        ]
      }
    };

    $http.get('../ws/instants/query?columns='+key+'&from=2017-09-30T00:00&intervalSeconds=300')
        .then(function(response) {
            $scope.history=[];

            // series
            var spl = key.split(",");
            for (var i=0; i<spl.length; i++) {
                $scope.series.push(spl[i]);
                $scope.data.push([]);
            }

            for (var i=0; i<response.data.length;i++) {
                var iv = response.data[i]
                $scope.labels.push($scope.toHumanDate(iv.timestamp.seconds));
                for (var j=0; j<$scope.series.length; j++) {
                    var iv2 = iv.values[$scope.series[j]];
                    $scope.data[j].push(iv2.value);
                    $scope.history.push(iv2);
                }
            }
        });


        $scope.toHumanDate = function(ts) {
            //var d = new Date(ts*1000);
            return moment(ts).format('DD.MM.YYYY, HH:mm:ss');
        };

        $scope.filter = function(object, field, filter) {
           if (!object) return {};
           if (!filter) return object;

           var filteredObject = {};
           Object.keys(object).forEach(function(key) {
             if (object[key][field].includes(filter)) {
               filteredObject[key] = object[key];
             }
           });

           return filteredObject;
         };


}]);
