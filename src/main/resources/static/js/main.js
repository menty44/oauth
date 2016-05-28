var app = angular.module("app",['ngRoute','angularMoment']);


/**
 *  Controller for user authentication webservice calls, and non controller specific methods
 */
app.controller("home",function($scope,$http,$rootScope){
    var self = this;
    self.authenticated = null;
    self.id = null;

    //Authenticate user if not authenticated
    if(!self.authenticated)
    $http.get("/user").success(function(data){

        self.user = data.name;
        self.id = data.id;
        self.authenticated = true;
        $rootScope.userId = self.id;

        $rootScope.$emit("CallReloadDash",self.id);

    }).error(function(){
        self.user = "N/A";
        self.authenticated = false;
    });

    //Picture viewer
    $scope.bigPicture = function($event){
        var postid = event.target.getAttribute('data-postid');
        var overlay = document.getElementById('overlay');
        var bigpic = document.getElementById('bigpicture');
        bigpic.src = "/getPictureForId//"+postid;
        overlay.style.display = 'block';
    };

    //Dissolve picture
    $scope.dissolve = function ($event) {
        document.getElementById('overlay').style.display = 'none';
    };

    //logout
    self.logout = function() {
        $http.post('logout', {}).success(function() {
            self.authenticated = false;
        }).error(function(data) {
            console.log("Logout failed")
            self.authenticated = false;
        });
    };

    //Search for users
    $scope.search = function($evt){
        var searchbar = document.getElementById('searchbar');

        if(!searchbar.value)
            document.getElementById('suggestions').style.display = "none";
        else{
            $http.get('/search/'+searchbar.value).success(function(data){
                if(data != null){
                    var length = data.length;
                    var i;
                    var ul = document.getElementById('suggestions_ul');
                    ul.innerHTML = "";
                    var child = "";
                    for(i = 0; i < length; i++ ){
                        child += '<a href="#/profile/'+data[i][0]+'"><li><img ng-src="/getProfilePicture/'+data[i][0]+'/" src="/getProfilePicture/'+data[i][0]+'/" />'+data[i][1]+'</li></a>';
                    }
                    ul.innerHTML = child;
                    document.getElementById('suggestions').style.display = "block";
                }
            });
        }
    };

    /**
     * Dashboard related webservice calls, like, delete, donwload posts etc
      */
}).controller('dash',function($scope,$http,$rootScope){

    $scope.id = $rootScope.userId;

    $rootScope.$on("CallReloadDash",function(event,data){
        $scope.id = data;
        $scope.reloadDash();
    });
    //Get posts data
    $scope.reloadDash = function(){
        var param = '/'+$rootScope.userId;
        var path = param.concat('/dash');
        $http.get(path).success(function(data){
            $scope.posts = data;
            var arrayLength = $scope.posts.length;
            for(var i = 0; i < arrayLength ; i++) {
                getPostLikeCountById(i);
            }
        }).error(function(){
            return '';
        });
    };

    if($rootScope.userId)
        $scope.reloadDash();


    //Get the number of likes for each photo
    var getPostLikeCountById = function(i){
        $http.get($scope.id + '/likes/info/'+$scope.posts[i].photoId).success(function (d) {
            $scope.posts[i].likes = d.count;
            document.getElementById('heart-'+$scope.posts[i].photoId).className += ' '+d.class;
            isPostBelongToId(i);
        }).error(function () {
            return '';
        });
    };

    //True if the given post belongs to the user, so a bin icon appers and the user is able to delete post
    var isPostBelongToId = function (id) {

        $http.get($scope.id+'/post/belongstouser/'+$scope.posts[id].photoId).success(function(data){
            $scope.posts[id].belongs = data.belongs;
        }).error(function(){
            return ' ';
        });
        return false;
    };

    //If the post was liked by user, it should appear as red
    $scope.setPostLikeById = function($event){
        var postid = event.target.getAttribute('data-postid');
        $http.post($scope.id+'/likes/set/'+postid,{}).success(function(data){
            if(data.action == 'like'){
                $event.target.className += ' heart-liked';
            }else{
                $event.target.className = ' ';
                $event.target.className = 'glyphicon glyphicon-heart-empty btn-lg';
            }
            $http.get($scope.id + '/likes/info/'+postid).success(function (d) {
                document.getElementById('likes-'+postid).innerHTML = d.count + ' likes';
            }).error(function () {
                return '';
            });

        }).error(function(){
            return '';
        });
    };


    //removes post and picture from database
    $scope.removePostById = function($event){
        var postid = event.target.getAttribute('data-postid');

        $http.delete($scope.id + '/post/remove/'+postid).success(function(d){

            if(d.success == true){
                (elem=document.getElementById('post-'+postid)).parentNode.removeChild(elem);
            }

        }).error(function(){
            return ' ';
        });

    };
    /**
     * Profile related webservice calls
     */
}).controller('profile', function($scope,$routeParams,$http,$rootScope){

    $scope.id = $routeParams.id;
    var profilePath = '/'+$scope.id+'/profile';


    //Download user data
    $scope.myProfile = $rootScope.userId == $routeParams.id;
    $http.get(profilePath).success(function(data){
        $scope.date = data.dateRegistered;
        $scope.desc = data.description;
        $scope.email = data.email;
        $scope.fbId = data.facebookId;
        $scope.following = data.following;
        $scope.name = data.name;
        $scope.photos = data.photos;
        $scope.profilePicturePath = data.profilePicturePath;
        $scope.website = data.website;

        //If user follows the user belonging to the profile, display UNFOLLOW button, if not, display FOLLOW button
        if(!$scope.myProfile){
            $http.get('/'+$rootScope.userId+'/isfollowing/'+$scope.id).success(function(data){

                var btn = document.getElementById('followBtn');
                if(data.following == true){
                    $scope.followBtnTxt = 'FOLLOW';
                }else{
                    $scope.followBtnTxt = 'UNFOLLOW';
                }
                setClass(btn,$scope.followBtnTxt,"UNFOLLOW",' btn_unfollow',' follow_btn');
            }).error(function(data){
                console.log("error "+data);
            });
        }

    }).error(function(data){
        console.log('error-e? '+data);
    });


    $http.get('/'+$scope.id+'/posts/count').success(function(data){
        $scope.postsCount = data.count;
    }).error(function(data){
        console.log('hiba '+data);
    });

    $http.get('/'+$scope.id+'/following/count').success(function(data){
        $scope.followingCount = data.count;
    }).error(function(data){
        console.log('hiba '+data);
    });

    $http.get('/'+$scope.id+'/follower/count').success(function(data){
        $scope.followerCount = data.count;
    }).error(function(data){
        console.log('hiba '+data);
    });

    $scope.showModal = false;
    $scope.toggleModal = function(){
        $scope.showModal = !$scope.showModal;
    };

    //follow or unfollow user, depending on previous state, change button labels
    $scope.followAction = function(){
        $http.get('/'+$rootScope.userId+'/followAction/'+$scope.id).success(function(data){
            var btn = document.getElementById('followBtn');
            $scope.followBtnTxt = data.label;
            setClass(btn,data.label,"UNFOLLOW",' btn_unfollow',' follow_btn');
        }).error(function(data){
            console.log('hiba '+data);
        });
    };

    function setClass(element,label,str,class1,class2){
        if(label == str){
            element.className = "";
            element.className += class1;
        }else{
            element.className ="";
            element.className += class2;
        }
    }

    //Edit profile data
    $scope.submit = function(){
        var formData = new FormData();
        formData.append('email',document.getElementById('email').value);
        formData.append('desc',document.getElementById('desc').value);
        formData.append('website',document.getElementById('website').value);

        $http.post('/updateProfile/'+$scope.id,formData,{
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        }).success(function(data){
            document.getElementById('d_desc').innerHTML =  document.getElementById('desc').value;

            if(data.email_error){
                document.getElementById("email_error").innerHTML = data.email_error;
            }else{
                document.getElementById("email_error").innerHTML = "";
            }

            if(data.website_error){
                document.getElementById("website_error").innerHTML = data.website_error;
            }else{
                document.getElementById('d_website').innerHTML =  document.getElementById('website').value;
                document.getElementById('d_website').src = 'http://'+document.getElementById('website').value;
                document.getElementById("website_error").innerHTML = "";
            }

        }).error(function(data){
            console.log(data);
        });

    };

    //Upload profile picture, append DOM accordingly
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

    $scope.showNewPostModal = false;
    $scope.newPostModal = function(){
        $scope.showNewPostModal = !$scope.showNewPostModal;
    };

    //"Upload" new post (picture)
    $scope.postAction = function(){

        var img = document.getElementById('newPhoto').files[0];
        var caption = document.getElementById('caption').value;
        var formData = new FormData();
        formData.append('image',img);
        formData.append('name',img.name);
        formData.append('caption',caption);

        $http.post('/uploadPicture/'+$scope.id,formData,{
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        }).success(function(data){
            if(data){
                var grid = document.getElementById('photo-grid-ul');
                var child = '<li><a href=""><img src="/getPictureForId/'+data.id+'/" ng-src="/getPictureForId/'+data.id+'/"/></a></li>';

                grid.innerHTML = child + grid.innerHTML;
            }
        });

    };
}).config(function($locationProvider,$routeProvider) {
    $routeProvider.when('/',{
        templateUrl: 'home.html',
        controller: 'dash'
    }).when('/profile/:id', {
        templateUrl: 'profile.html',
        controller: 'profile'
    }).otherwise({redirectTo:'/'});
});

//Modal view
app.directive('modal',function(){
   return{
       restrict:'E',
       replace:true,
       transclude:true,
       scope:true,
       link:function(scope,element,attrs){

           scope.title = attrs.title;
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
