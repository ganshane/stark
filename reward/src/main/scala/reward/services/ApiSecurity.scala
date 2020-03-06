package reward.services

import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.{AuthenticationProvider, UsernamePasswordAuthenticationToken}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.{UserDetails, UserDetailsService}
import org.springframework.security.core.{Authentication, GrantedAuthority}
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter
import reward.RewardConstants
import reward.entities.OnlineUser
import stark.activerecord.services.DSL.update

import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-05
  */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true,securedEnabled = true)
class ApiSecurity extends WebSecurityConfigurerAdapter{
  private val logger = LogFactory.getLog(getClass());
  @Autowired
//  var authenticationManager:AuthenticationManager = _
  override def configure(auth: AuthenticationManagerBuilder): Unit = {
    auth.authenticationProvider(new AuthenticationProvider(){
      override def authenticate(authentication: Authentication): Authentication = {
        val bearer = authentication.asInstanceOf[BearerTokenAuthenticationToken]
        val token = bearer.getToken
        val num = update[OnlineUser] set (expiredAt=DateTime.now.plusMinutes(30)) where OnlineUser.token === token execute

        if(num == 1) {
          val user = OnlineUser.find_by_token(token).head.user
          var roles = List[GrantedAuthority](new SimpleGrantedAuthority(RewardConstants.ROLE_USER))
          if(user.isAdmin == 1){
            roles :+= new SimpleGrantedAuthority(RewardConstants.ROLE_ADMIN)
          }
          new UsernamePasswordAuthenticationToken(user,user,roles)
        }else {
//          throw new AuthenticationCredentialsNotFoundException("用户未找到,重新登录")
          logger.debug("user not found")
          authentication
        }
      }
      override def supports(authenticationClass: Class[_]): Boolean = {
        authenticationClass.isAssignableFrom(classOf[BearerTokenAuthenticationToken])
      }
    })
    auth.userDetailsService(new UserDetailsService(){
      override def loadUserByUsername(username: String): UserDetails = {
        throw new UnsupportedOperationException("unsupported !!")
      }
    })
//    super.configure(auth)
  }
  @Bean
  def authenticationJwtTokenFilter() ={
    new BearerTokenAuthenticationFilter(authenticationManager)
  }
  override def configure(http: HttpSecurity): Unit = {
    http.csrf().disable()
//      .authorizeRequests()
//      .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**")
//      .permitAll()
//      .anyRequest().authenticated().and()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
     http.addFilterBefore(authenticationJwtTokenFilter(),classOf[BearerTokenAuthenticationFilter])

  }
}
