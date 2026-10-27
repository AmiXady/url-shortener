create user redirect_reader with PASSWORD 'redirect_reader';

grant connect on DATABASE url_shortener TO redirect_reader;
grant USAGE on SCHEMA public TO redirect_reader;

alter DEFAULT PRIVILEGES FOR ROLE url_shortener IN SCHEMA public
    grant select on TABLES to redirect_reader;