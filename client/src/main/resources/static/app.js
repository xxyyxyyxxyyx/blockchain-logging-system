new Vue({
    el: "#app",
    data: {
        logs: [],
        log: {
            application: "",
            event: "",
            message: ""
        }
    },
    methods: {
        emptyData: function () {
            this.logs = [],
                this.log = {
                    application: "",
                    event: "",
                    message: ""
                };
        },
        getCurrentLog: function () {
            let application = this.log.application;
            axios
                .get(`http://magento1.vagrant.com:8080/api/logs/${application}`)
                .then(response => (this.logs = response.data))
        .catch(error => console.log(error));

            this.log.application = "";
        },
        getLogHistory: function () {
            let application = this.log.application;
            axios
                .get(`http://magento1.vagrant.com:8080/api/logs/history/${application}`)
                .then(response => (this.logs = response.data))
        .catch(error => console.log(error));
            this.log.application = "";

        },
        createLog: function () {
            axios
                .post("http://magento1.vagrant.com:8080/api/logs", this.log)
                .then(response => {
                console.log(response);
            this.log = {
                application: "",
                event: "",
                message: ""
            };
        })
        .catch(error => console.log(error));
        }
    }
});
