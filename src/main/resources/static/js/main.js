var app = angular.module("app",['ngRoute']);

app.controller("home",function($scope,$http){
    var self = this;
    self.authenticated = null;
    self.id = null;
    $http.get("/user").success(function(data){
        self.user = data.name;
        self.id = data.id;
        self.authenticated = true;

        var str = '/'+self.id;
        var str2 = str.concat('/dash');
        $http.get(str2).success(function(data){
            self.posts = data;
        }).error(function(){
            return '';
        });

    }).error(function(){
        self.user = "N/A";
        self.authenticated = false;
    });

    self.logout = function() {
        $http.post('logout', {}).success(function() {
            self.authenticated = false;
        }).error(function(data) {
            console.log("Logout failed")
            self.authenticated = false;
        });
    };
}).config(function($locationProvider,$routeProvider) {
        $routeProvider.when('/',{
            templateUrl: 'home.html',
            controller: 'home'
        }).when('/profile/:id',{
            templateUrl: 'profile.html',
            controller: 'profile'
        }).otherwise({redirectTo:'/'});

}).controller('profile',function($scope,$routeParams,$http){
    $scope.id = $routeParams.id;
    var str = '/'+$scope.id+'/profile';

    $http.get(str).success(function(data){
        $scope.date = data.dateRegistered;
        $scope.desc = data.description;
        $scope.email = data.email;
        $scope.fbId = data.facebookId;
        $scope.following = data.following;
        $scope.name = data.name;
        $scope.photos = data.photos;
        $scope.profilePicturePath = data.profilePicturePath;
        $scope.website = data.website;
    }).error(function(data){
        console.log('error-e? '+data);
    });

    $scope.showModal = false;
    $scope.toggleModal = function(){
        $scope.showModal = !$scope.showModal;
    };

    $scope.submit = function(){
        var formData = new FormData();
        formData.append('email',document.getElementById('email').value);
        formData.append('desc',document.getElementById('desc').value);
        formData.append('website',document.getElementById('website').value);

        $http.post('/updateProfile/'+$scope.id,formData,{
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        }).success(function(data){
            alert("succes");
        }).error(function(data){
            console.log(data);
        });

    };

    $scope.upload = function(){
        var img = document.getElementById('profile').files[0];
        var formData = new FormData();
        formData.append('name',img.name);
        formData.append('img',img);

        $http.post('/uploadProfilePicture/'+$scope.id,formData,{
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        }).success(function(data){
             document.getElementById('profilePic').src = "/getProfilePicture/"+$scope.id;
        }).error(function(data){
            console.log(data);
        });
    };
});


app.directive('modal',function(){
   return{
       restrict:'E',
       replace:true,
       transclude:true,
       scope:true,
       link:function(scope,element,attrs){
           scope.title = "Edit Profile";

           scope.$watch(attrs.visible,function(value){
              if(value == true)
                  $(element).modal('show');
               else
                  $(element).modal('hide');
           });

           $(element).on('show.bs.modal',function(){
              scope.$apply(function(){
                 scope.$parent[attrs.visible] = true;
              });
           });

           $(element).on('show.bs.modal',function(){
              scope.$apply(function(){
                  scope.$parent[attrs.visible] = false;
              });
           });
       },
       template:
       '<div class="modal fade">' +
       '<div class="modal-dialog">' +
       '<div class="modal-content">' +
       '<div class="modal-header">' +
       '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
       '<h4 class="modal-title">{{ title }}</h4>' +
       '</div>' +
       '<div class="modal-body" ng-transclude></div>' +
       '</div>' +
       '</div>' +
       '</div>'
   };
});
