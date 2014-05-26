angular.module('imageServerAdmin', ['ngRoute', 'angularFileUpload']);

angular.module('imageServerAdmin')
    .directive('ngEnter', function () {
        return function (scope, element, attrs) {
            element.bind("keydown keypress", function (event) {
                if (event.which === 13) {
                    scope.$apply(function () {
                        scope.$eval(attrs.ngEnter, {'event': event});
                    });

                    event.preventDefault();
                }
            });
        };
    })

    .factory('imageService', ['$http', '$rootScope', function ($http, $rootScope) {
        var imageInfo = function (externalId, callback) {
            $http.get('/imageInfo?token=' + $rootScope.token + '&iid=' + externalId).success(function (data) {
                callback(data.result);
            });
        };

        var imageContexts = function (callback) {
            $http.get('/context/list?token=' + $rootScope.token).success(function (data) {
                callback(data.result);
            });
            // callback(['ONLINE', 'DIGITAL'])
        };

        var imageResolutions = function (context, callback) {
            $http.get('/modification/listResolutions?token=' + $rootScope.token + '&context=' + context).success(function (data) {
                callback(data.result);
            });
        };

        var registeredModifications = function (externalId, context, callback) {
            $http.get('/modification/listModifications?token=' + $rootScope.token + '&context=' + context + '&iid=' + externalId).success(function (data) {
                callback(data.result);
            });
        };

        var imageTypes = function (callback) {
            callback(['JPEG', 'PNG']);
        };

        var resolutionDetails = function (resolutionId, callback) {
            $http.get('/modification/resolutionDetails?token=' + $rootScope.token + '&id=' + resolutionId).success(function (data) {
                callback(data.result);
            });
        };

        return {
            getImageInfo: imageInfo,
            getContexts: imageContexts,
            getImageResolutions: imageResolutions,
            getImageTypes: imageTypes,
            getImageModifications: registeredModifications,
            getResolutionDetails: resolutionDetails
        };
    }])

    .controller('ListResolutionsController', [ 'imageService', '$rootScope', '$scope', function (imageService, $rootScope, $scope) {
        $rootScope.selectedMenu = 'resolutions';

        $scope.selectContext = function (context) {
            $scope.selectedContext = context;
            $rootScope.preferredContext = context;

            imageService.getImageResolutions($scope.selectedContext, function (data) {
                $scope.resolutions = data;
            });
        };

        imageService.getContexts(function (data) {
            $scope.contexts = [''].concat(data);
            console.log($scope.contexts);

            if (!$rootScope.preferredContext) {
                $scope.selectContext($scope.contexts[0]);
            }
            else {
                $scope.selectContext($rootScope.preferredContext);
            }
        });
    }])

    .controller('ManageResolutionController', [
        'imageService', '$http', '$routeParams', '$rootScope', '$scope',
        function (imageService, $http, $routeParams, $rootScope, $scope) {
            $rootScope.selectedMenu = 'resolutions';

            $scope.feedback = null;
            $scope.selectedContexts = [];
            $scope.resolution = {'id': 0, 'name': '', 'width': 0, 'height': 0, 'tags': ['']};

            if ($routeParams.resolutionId > 0) {
                // Load existing resolution
                imageService.getResolutionDetails($routeParams.resolutionId, function (data) {
                    $scope.setResolution(data);
                });
            }

            imageService.getContexts(function (data) {
                $scope.contexts = data;
            });

            $scope.setResolution = function (data) {
                $scope.resolution = data.resolution;

                if ($scope.resolution.tags.length < 1) {
                    $scope.resolution.tags.push('');
                }

                var list = data.context ? data.context : [];
                $scope.selectedContexts = {};
                angular.forEach(list, function (value) {
                    $scope.selectedContexts[value] = true;
                });
            };

            $scope.save = function () {
                var formData = { 'resolution': $scope.resolution, 'context': [] };

                angular.forEach($scope.contexts, function (value) {
                    if ($scope.selectedContexts[value]) {
                        formData.context.push(value);
                    }
                }, formData);

                $http.post('/modification/updateResolution?token=' + $rootScope.token, formData)
                    .success(function (data) {
                        if (!data.success) {
                            $scope.feedback = { 'type': 'alert-danger', 'message': 'Something went wrong, resolution has not been saved.'};
                        } else {
                            // if successful, bind success message to message
                            $scope.feedback = { 'type': 'alert-success', 'message': 'Resolution has been saved successfully.'};
                            $scope.setResolution(data.result);
                        }
                    }).error(function (data) {
                        $scope.feedback = { 'type': 'alert-danger', 'message': data.errorMessage};
                    });
            };
        }])

    .controller('ImageViewController', ['imageService', '$routeParams', '$rootScope', function (imageService, $routeParams, $rootScope) {
        $rootScope.selectedMenu = 'view';

        this.externalId = '';
        this.feedback = 'Please enter the external id of the image you wish to view.';
        this.image = null;

        this.contexts = null;
        this.imageTypes = null;
        this.resolutions = null;

        this.selectedContext = null;
        this.selectedImageType = null;
        this.selectedResolution = null;
        this.selectedCrop = null;

        this.previewUrl = '';

        var $controller = this;

        this.loadImage = function () {
            $controller.feedback = '';
            $controller.image = null;

            var iid = this.externalId;

            if (iid) {
                imageService.getImageInfo(iid, function (data) {
                    $controller.image = data;

                    if (!data.existing) {
                        $controller.feedback = 'Image with external id ' + iid + ' was not found.';
                    }

                    $controller.buildPreviewUrl();
                });

                $controller.updateModifications();
            }
            else {
                $controller.feedback = 'Please enter the external id of the image you wish to view.';
            }
        };

        this.selectContext = function (context) {
            $controller.selectedContext = context;

            imageService.getImageResolutions(context, function (data) {
                $controller.resolutions = data;
                $controller.selectResolution(data[0]);
                $controller.updateModifications();
            });
        };

        this.updateModifications = function () {
            if ($controller.externalId) {
                imageService.getImageModifications($controller.externalId, $controller.selectedContext, function (data) {
                    $controller.modifications = data;
                    $controller.buildPreviewUrl();
                });
            }
            else {
                $controller.modifications = [];
            }
        };

        this.selectImageType = function (imageType) {
            $controller.selectedImageType = imageType;
        };

        this.selectResolution = function (resolution) {
            $controller.selectedResolution = resolution;
            $controller.buildPreviewUrl();
        };

        this.buildPreviewUrl = function () {
            if (this.image) {
                this.selectedCrop = this.getCrop(this.selectedResolution);
                this.previewUrl = '/view?iid=' + this.image.externalId + '&imageType=' + this.selectedImageType
                    + '&width=' + this.selectedResolution.width
                    + '&height=' + this.selectedResolution.height
                    + '&context=' + this.selectedContext;
            }
            else {
                this.previewUrl = '';
                this.selectedCrop = null;
            }
        };

        this.getCrop = function (resolution) {
            var found = null;

            angular.forEach(this.modifications, function (value) {
                if (resolution.width == value.resolution.width && resolution.height == value.resolution.height) {
                    found = value;
                }
            }, $controller);

            return found;
        };

        imageService.getContexts(function (data) {
            $controller.contexts = data;
            $controller.selectContext(data[0]);
        });

        imageService.getImageTypes(function (data) {
            $controller.imageTypes = data;
            $controller.selectImageType(data[0]);
        });

        if ($routeParams.externalId) {
            this.externalId = $routeParams.externalId;
            this.loadImage();
        }
    }])

    .controller('ImageUploadController', ['imageService', '$location', '$upload', '$rootScope', function (imageService, $location, $upload, $rootScope) {
        $rootScope.selectedMenu = 'upload';

        this.externalId = '';
        this.file = null;
        this.existing = false;

        var $controller = this;

        this.selectFile = function ($file) {
            this.file = $file;
        };

        this.loadImageFeedback = function () {
            imageService.getImageInfo(this.externalId, function (data) {
                $controller.existing = data.existing;
            });
        };

        this.upload = function () {
            $upload.upload({
                url: '/load',
                data: {
                    'token': $rootScope.token,
                    'iid': this.externalId
                },
                fileFormDataName: ['imageData'],
                file: this.file
            }).success(function (data, status, headers, config) {
                $location.path('/view/' + $controller.externalId);
            });
        };

        this.canUpload = function () {
            return !this.existing && this.externalId && this.file;
        };
    }]);

